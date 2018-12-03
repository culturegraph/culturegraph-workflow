/*
 * MIT License
 *
 * Copyright (c) 2018 Deutsche Nationalbibliothek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Groovy: Version 2.5.0
 */
package org.culturegraph.script

import groovy.cli.picocli.CliBuilder
import org.culturegraph.workflow.core.entities.InputFormat
import org.culturegraph.workflow.core.entities.OutputFormat
import org.culturegraph.workflow.core.entities.Transformer
import org.culturegraph.workflow.plugin.io.DecompressedInputStream
import org.culturegraph.workflow.plugin.io.MarcStreamFactory
import org.culturegraph.workflow.plugin.metafacture.MetamorphTransformerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import java.util.stream.Stream

def summary = '\n' +
        'Workflow that transform MARC21 or MARCXML (plain or gz) into csv rows. ' +
        'Each row contains a record id followed by the generated keys for each record. ' +
        'Rows without keys will be removed.' +
        '\n'

def usage = this.class.getSimpleName() + ' ' +
        '[-h] --input FILE --morph FILE [--morph FILE ...] [--threads NUM]'

def cli = new CliBuilder(usage: usage, header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'File in MARC21 or MARCXML format (plain or gzip)', type: String.class, required: true, defaultValue: '-'
    m argName: 'file', longOpt: 'morph', 'Metamorph definition files', type: String.class, required: true
    t argName: 'num', longOpt: 'threads', 'Number of threads', type: String.class, required: true, defaultValue: '1'
    h longOpt: 'help', 'Show usage information'
}

def options = cli.parse(args)

if (!options) {
    return
}

if (options.h) {
    cli.usage()
    return
}

boolean useStdin = options.i == '-'
def morphDefList = options.ms
def threads = options.t as Integer

// Matches (DE-101)1234567 key1 ...
Pattern idWithAtLeastOneKey = Pattern.compile('^\\(\\S+\\)\\S+\\s.+')

SynchronousQueue<String> readQueue = new SynchronousQueue<>()
SynchronousQueue<Optional<String>> writeQueue = new SynchronousQueue<>()
ExecutorService pool = Executors.newFixedThreadPool(threads)
for (int i = 0; i < threads; i++) {
    pool.execute(new Runnable() {
        @Override
        void run() {
            MetamorphTransformerFactory transformerFactory = new MetamorphTransformerFactory(InputFormat.MARC21, OutputFormat.CSV, morphDefList)
            Transformer transformer = transformerFactory.newTransformer()
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String item = readQueue.take()
                    Optional<String> transformedItem = transformer.transform(item)
                    writeQueue.put(transformedItem)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
    })
}


System.out.withWriter 'UTF-8', { new BufferedWriter(it).withCloseable { writer ->

    Thread consumer = new Thread(new Runnable() {
        @Override
        void run() {
            try {
                // Note: Replace this in Java 9
                //       map { opt -> if ... } ==> takeWhile(opt -> opt == null)
                Stream.generate { writeQueue.poll(10, TimeUnit.SECONDS) }
                        .map { opt -> if (opt == null) throw new InterruptedException() else return opt }
                        .filter { opt -> opt.isPresent() }
                        .map { opt -> opt.get() }
                        .filter(idWithAtLeastOneKey.asPredicate())
                        .forEach { s -> writer.write(s + '\n') }
            } catch (all) {
                Thread.currentThread().interrupt()
            }
        }
    })

    consumer.start()

    // Matches strings with leading whitespace
    Pattern startsWithWhitespace = Pattern.compile('^\\s+')
    // Matches  strings that contains newline or carriage return
    Pattern containsNewlineOrCarriageReturn = Pattern.compile('[\\n\\r]')

    MarcStreamFactory streamFactory = new MarcStreamFactory()
    InputStream inputStream  = useStdin ? System.in : new FileInputStream(options.i as String)
    streamFactory.newRecordStream(DecompressedInputStream.of(inputStream))
            .map { s -> startsWithWhitespace.matcher(s).replaceAll('') }
            .map { s -> containsNewlineOrCarriageReturn.matcher(s).replaceAll(' ') }
            .forEach { s -> readQueue.put(s) }

    consumer.join()
    pool.shutdownNow()
}}
