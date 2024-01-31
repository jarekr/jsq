package jarekr.jsq;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or

// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;

import static jarekr.jsq.Lib.*;

@Command(name = "jsq", mixinStandardHelpOptions = true, version = "1.0",
        description = "java squish - (de)compresses given file ")
class Jsq implements Callable<Integer> {

    @Parameters(index = "0", description = "The file to (de)squish.")
    private File file;

    @Option(names = {"-x", "--extract"}, description = "extract instead of squishing")
    private boolean extract = false;

    @Option(names = {"-o", "--output"}, description = "file to output to")
    private String outfile;

    @Option(names = {"--header"}, description = "dump header of compressed file")
    private boolean showHeader = false;

    private int squishFile(File f) throws IOException {

        System.out.println("Reading in "+file.toPath());
        byte[] fileContents = Files.readAllBytes(file.toPath());
        Map<Byte, Integer> freqmap = countBytes(fileContents);

        // shift off the lowest freq having key/value entries and create HNode
        // insert HNode back into sorted list, sorting by it's new Value
        // repeat both steps until 1 or 0 nodes remain;
        // when 1 remains, it becomes the root node
        // if 0 nodes remain, error??

        HNode head = buildTree(freqmap);
        //System.out.println("Built tree with head == "+head.kharToString() +" "+head.freq);

        Map<Byte, HNode> byChar = derivePrefixes(head);

        //for (Map.Entry<Byte, Integer> e: freqmap.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).toList()) {
        //    HNode n = byChar.get(e.getKey());
        //    System.out.println(" |"+n.kharToString()+"|  "+ n.prefix.toString()+ "  f="+n.freq);
        //}

        String newFileName = outfile;
        if (newFileName == null) {
            newFileName = file.getName() + ".jsq";
        }

        System.out.println("squishing...");
        BitVector compressed = compress(fileContents, byChar);

        JsqHeader header = new JsqHeader(toByPrefix(byChar), (int)compressed.getBitCount(), 0);
        System.out.println("squished "+fileContents.length+" bytes down to "+compressed.toIntList().size() * 4 + " bytes");
        byte[] headerBytes = header.generate();
        System.out.println("  header "+headerBytes.length+" bytes");
        writeOutToFile(newFileName, header.generate(), compressed);

        return 0;
    }

    private int printHeader(File f) throws IOException {
        System.out.println("Reading "+file.toPath());
        byte[] fileContents = Files.readAllBytes(file.toPath());
        JsqHeader header = JsqHeader.parseHeader(fileContents);
        if (header == null) {
            System.err.println("provided file "+file.toPath()+" is not a .jsq file, or header is corrupted");
            return 2;

        }

        System.out.printf("byte count=%d, bit count=%d, unique chars=%d%n", header.getHeaderByteCount(),
                header.getBitCount(), header.prefixToChar().size());
        for (var entry: header.prefixToChar().entrySet()) {
            String khar = "";
            byte value = entry.getValue();
            String hexValue = String.format("0x%02x", value);
            if (Character.isValidCodePoint(value) && (value < 8 || value > 15)) {
                khar = String.format("'%c'", value);
            } else {
                khar = "???";
            }
            System.out.printf("[%s] %s %s%n", hexValue, khar, entry.getKey());
        }

        return 0;
    }

    private int unSquishFile(File f) throws IOException {

        System.out.println("Reading "+file.toPath());
        byte[] fileContents = Files.readAllBytes(file.toPath());
        JsqHeader header = JsqHeader.parseHeader(fileContents);
        if (header == null) {
            System.err.println("provided file "+file.toPath()+" is not a .jsq file, or header is corrupted");
            return 2;

        }
        ByteBuffer buffer = ByteBuffer.wrap(fileContents, header.getHeaderByteCount(), fileContents.length - header.getHeaderByteCount());

        BitVector compressed = readFromByteArray(buffer.array(), header);

        List<Byte> results = decompress(compressed, header.prefixToChar());
        byte[] bytes = new byte[results.size()];
        int i = 0;
        for (byte b: results) {
            bytes[i] = b;
            ++i;
        }
        ByteBuffer buff = ByteBuffer.wrap(bytes);
        CharBuffer output = StandardCharsets.UTF_8.decode(buff);

        if (outfile != null) {
            System.out.println("outputting "+bytes.length+" bytes to "+outfile);
            try (BufferedWriter outwriter = Files.newBufferedWriter(Path.of(outfile), StandardCharsets.UTF_8)) {
                outwriter.write(output.array());
            } catch (IOException exc) {
                System.err.format("IOException: %s%n", exc);
            }
        } else {
            System.out.println("outputting "+bytes.length+" bytes");
            System.out.println(output);
        }
        return 0;
    }

    @Override
    public Integer call() throws Exception { // your business logic goes here...

        if (!file.exists()) {
            System.err.println("file not found ");
            return 2;
        }
        int exit = 0;
        try {
            if (showHeader) {
                exit = printHeader(file);
            } else if (extract) {
                exit = unSquishFile(file);
            } else {
                exit = squishFile(file);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return exit;
    }

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        var jsq = new Jsq();
        int exitCode = new CommandLine(jsq).execute(args);
        System.exit(exitCode);
    }
}
