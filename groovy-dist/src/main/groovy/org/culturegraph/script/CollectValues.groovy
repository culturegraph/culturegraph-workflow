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
 
import groovy.cli.picocli.CliBuilder
import org.culturegraph.workflow.core.KeyValueCollector
import org.culturegraph.workflow.plugin.io.DecompressedInputStream

def summary = '\n' +
        'Workflow that processes a key-value list. ' +
        'All values with the same key will be collected in a single row. ' +
        'Each row is prefixed with the number of values of the row.' +
        '\n'

def usage = this.class.getSimpleName() + ' ' +
        '[-h] --input CSV-FILE [--separator DELIMITER] --output CSV-FILE'

def cli = new CliBuilder(usage: usage, header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'Input csv file. Contains "key value" per line.', type: String.class, defaultValue: '-'
    o argName: 'file', longOpt: 'output', 'Output csv file. Contains "count values" per line.', type: String.class, defaultValue: '-'
    s argName: 'delimiter', longOpt: 'separator', 'CSV separator that separates the key-value pairs in the input file. Default: SPACE.', type: String.class, defaultValue: ' '
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

String separator = options.s as String

def useStdin = ((String) options.i) == '-'
def useStdout = ((String) options.o) == '-'

InputStream inputStream = useStdin ? System.in : new FileInputStream(options.i as String)
OutputStream outputStream = useStdout ? System.out : new FileOutputStream(options.o as String)

outputStream.withWriter 'UTF-8', {new BufferedWriter(it).withCloseable { writer ->

    inputStream.withReader 'UTF-8', { new BufferedReader(it).withCloseable { reader ->
        Iterator<String> lines = reader.lines().iterator()
        KeyValueCollector collector = new KeyValueCollector(lines, separator)
        while (collector.hasNext()) {
            String row = collector.next()
            writer.write(row + '\n')
        }

    }}  // reader end

}}  // writer end