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

import de.odysseus.staxon.xml.util.PrettyXMLEventWriter
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory

import groovy.cli.picocli.CliBuilder

def summary = '\n' +
        'Formats XML by adding indentation.\n'

def usage = this.class.getSimpleName() + ' ' + '[-ioh]'

def cli = new CliBuilder(usage: usage, header: '\nOptions:', footer: summary)
cli.with {
    i argName: 'file', longOpt: 'input', 'Input (Default: stdin)', type: String.class, defaultValue: '-'
    o argName: 'file', longOpt: 'output', 'Output (Default: stdout)', type: String.class, defaultValue: '-'
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

def useStdin = (options.i as String) == '-'
def useStdout = (options.o as String) == '-'

InputStream inputStream = useStdin ? System.in : new FileInputStream(options.i as String)
OutputStream outputStream = useStdout ? System.out : new FileOutputStream(options.o as String)

int KB64 = 65536
String utf8 = "UTF-8"

XMLInputFactory inputFactory = XMLInputFactory.newInstance()
XMLEventReader reader = inputFactory.createXMLEventReader(new BufferedInputStream(inputStream, KB64))
XMLOutputFactory outputFactory = XMLOutputFactory.newInstance()
XMLEventWriter writer = new PrettyXMLEventWriter(outputFactory.createXMLEventWriter(new BufferedOutputStream(outputStream, KB64), utf8))
writer.add(reader)
writer.flush()
writer.close()
reader.close()