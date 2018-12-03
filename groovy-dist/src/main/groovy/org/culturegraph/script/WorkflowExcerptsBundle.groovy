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
import org.culturegraph.workflow.core.BundleBuilder
import org.culturegraph.workflow.core.entities.ChainedTransformer
import org.culturegraph.workflow.core.entities.InputFormat
import org.culturegraph.workflow.core.entities.OutputFormat
import org.culturegraph.workflow.plugin.io.DecompressedInputStream
import org.culturegraph.workflow.plugin.metafacture.MetamorphTransformerFactory
import org.culturegraph.workflow.plugin.metafacture.XslTransformerFactory

def summary = '\n' +
        'Workflow that generates a Bundle of MARCXML excerpts from MARC21 records.' +
        '\n' +
        'Bundles excerpts by their clustering.'
        '\n'

def usage = this.class.getSimpleName() + ' ' +
        '[-h] --input FILE [--separator delimiter] [--output FILE] --morph FILE [--morph FILE ...] --xsl FILE [--xsl FILE ...]'

def cli = new CliBuilder(usage: usage, header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'Input file with mapped excerpts.', type: String.class, required: true
    o argName: 'file', longOpt: 'output', 'Output file. Use - for stdout. Default: stdout.', type: String.class, required: false, defaultValue: '-'
    a argName: 'name', longOpt: 'algorithm', 'Algorithm name. A annotation that will be inserted.', type: String.class, required: false, defaultValue: 'default'
    m argName: 'file', longOpt: 'morph', 'Metamorph definition files.', type: String.class, required: true
    x argName: 'file', longOpt: 'xsl', 'XSL Stylsheet files.', type: String.class, required: true
    s argName: 'delimiter', longOpt: 'separator', 'Separator used for input csv to split key and record. Default: SPACE.', type: String.class, defaultValue: ' '
    h longOpt: 'help', 'Show usage information.'
}

def options = cli.parse(args)

if (!options) {
    return
}

if (options.h) {
    cli.usage()
    return
}

boolean useStdout = (options.o as String) == '-'
def algorithm = options.a as String
def input = options.i as File
def separator = options.s as String
def morphDefList = options.ms
def stylesheets = options.xs

BundleBuilder bundleBuilder = new BundleBuilder(algorithm)

def marcToXmlTransformationFactory = new MetamorphTransformerFactory(InputFormat.MARC21, OutputFormat.XML, morphDefList)
def marcToXmlTransformer = marcToXmlTransformationFactory.newTransformer()
def stylesheetTransformerFactory = new XslTransformerFactory(stylesheets)
def stylesheetTransformer = stylesheetTransformerFactory.newTransformer()
def asXmlTransformer = new ChainedTransformer(marcToXmlTransformer, stylesheetTransformer)

OutputStream outputStream = useStdout ? System.out : new FileOutputStream(options.o as String)
outputStream.withWriter 'UTF-8', {new BufferedWriter(it).withCloseable { writer ->
    InputStream inputStream = DecompressedInputStream.of(new FileInputStream(input))
    inputStream.withReader { new BufferedReader(it).withCloseable { reader ->

        Iterator<String> mappedExcerptsIterator = reader.lines()
                .map { s -> s.split(separator, 2) }
                .filter { csv -> csv.length == 2 }
                .map { csv -> asXmlTransformer.transform(csv[1]).map { xml -> "<!-- ${csv[0]} -->" + xml } as Optional<String> }
                .filter { opt -> opt.isPresent() }
                .map { opt -> opt.get() }
                .iterator()

        writer.write(bundleBuilder.open())

        while (mappedExcerptsIterator.hasNext()) {
            String mappedExcerpt = mappedExcerptsIterator.next()
            writer.write(bundleBuilder.put(mappedExcerpt) + '\n')
        }

        writer.write(bundleBuilder.close())

    }}  // reader end

}}  // writer end