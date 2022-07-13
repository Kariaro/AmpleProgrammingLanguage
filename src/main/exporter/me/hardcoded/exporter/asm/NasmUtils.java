package me.hardcoded.exporter.asm;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.utils.DebugUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Used to call nasm
 */
public class NasmUtils {
	// TODO: Put in temp folder or unpackage in installation directory
	
	private static File getNasmFolder() {
		return new File("src/main/resources/tools/nasm-2.15.05/").getAbsoluteFile();
	}
	
	private static File getNasm() {
		return new File(getNasmFolder(), "nasm.exe");
	}
	
	public static byte[] compile(AmpleConfig config, byte[] bytes) {
		File outputFolder = config.getConfiguration().getOutputFolder();
		String inputFile = DebugUtils.getNextFileId(outputFolder, "out_%d.asm");
		String outputFile = DebugUtils.getNextFileId(outputFolder, "out_%d.elf");
		
		// Create a temporary output asm file with the assembler bytes
		try {
			Files.write(Path.of(inputFile), bytes);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		// Create a nasm process and compile the code
		try {
			new ProcessBuilder(
				getNasm().getAbsolutePath(),
				inputFile,
				"-f", "elf64",
				"-o", outputFile)
				.directory(outputFolder)
				.inheritIO()
				.start()
				.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			return Files.readAllBytes(Path.of(outputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
