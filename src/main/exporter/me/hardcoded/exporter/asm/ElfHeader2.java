package me.hardcoded.exporter.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ElfHeader2 {
	private final Elf64_Ehdr fileHeader;
	private final List<Elf64_Phdr> programHeaderList;
	private final List<Elf64_Shdr> sectionHeaderList;
	private final List<String> sectionNames;
	
	// Debug variables
	private final boolean debug;
	private final String fileName;
	private final List<String> strtabDebug;
	private final List<Elf64_Sym> symtabDebug;
	
	public ElfHeader2(boolean debug, String fileName) {
		this.fileHeader = new Elf64_Ehdr();
		this.programHeaderList = new ArrayList<>();
		this.sectionHeaderList = new ArrayList<>();
		this.sectionNames = new ArrayList<>();
		
		// Debug
		this.debug = debug;
		this.fileName = fileName;
		this.strtabDebug = new ArrayList<>();
		this.symtabDebug = new ArrayList<>();
	}
	
	public int addSectionName(String name) {
		int offset = 0;
		for (String str : sectionNames) {
			offset += str.length() + 1;
		}
		
		sectionNames.add(name);
		
		return offset;
	}
	
	public int addSymbolName(String name) {
		int offset = 0;
		for (String str : strtabDebug) {
			offset += str.length() + 1;
		}
		
		strtabDebug.add(name);
		
		return offset;
	}
	
	private void setupSectionHeaders() {
		{ // .null
			Elf64_Shdr shdr = new Elf64_Shdr("NULL");
			shdr.debug_name = ".null";
			sectionHeaderList.add(shdr);
			shdr.sh_name = Integer.toString(addSectionName(""));
			shdr.sh_type = Elf64_Shdr.SHT_NULL;
			shdr.sh_flags = 0;
			shdr.sh_addr = "0";
			shdr.sh_offset = "0";
			shdr.sh_size = "0";
			shdr.sh_link = "0";
			shdr.sh_info = "0";
			shdr.sh_addralign = "0";
			shdr.sh_entsize = "0";
		}
		
		List<Elf64_Shdr> debugList = new ArrayList<>();
		if (debug) {
			{ // .symtab
				Elf64_Shdr shdr = new Elf64_Shdr("SYMTAB");
				shdr.debug_name = ".symtab";
				debugList.add(shdr);
				shdr.sh_name = Integer.toString(addSectionName(".symtab"));
				shdr.sh_type = Elf64_Shdr.SHT_SYMTAB;
				shdr.sh_flags = 0;
				shdr.sh_addr = "0";
				shdr.sh_offset = "ELF64__SECTION_SYMTAB - $$";
				shdr.sh_size = "ELF64__SECTION_SYMTAB_SIZE";
				shdr.sh_link = "ELF64__SECTION_STRTAB_INDEX";
				shdr.sh_info = Integer.toString(symtabDebug.size()); // count of symbols
				shdr.sh_addralign = "8h";
				shdr.sh_entsize = "18h";
			}
			
			{ // .strtab
				Elf64_Shdr shdr = new Elf64_Shdr("STRTAB");
				shdr.debug_name = ".strtab";
				debugList.add(shdr);
				shdr.sh_name = Integer.toString(addSectionName(".strtab"));
				shdr.sh_type = Elf64_Shdr.SHT_STRTAB;
				shdr.sh_flags = 0;
				shdr.sh_addr = "0";
				shdr.sh_offset = "ELF64__SECTION_STRTAB - $$";
				shdr.sh_size = "ELF64__SECTION_STRTAB_SIZE";
				shdr.sh_link = "0";
				shdr.sh_info = "0";
				shdr.sh_addralign = "1h";
				shdr.sh_entsize = "0";
			}
		}
		
		{ // .text
			Elf64_Shdr shdr = new Elf64_Shdr("TEXT");
			shdr.debug_name = ".text";
			sectionHeaderList.add(shdr);
			shdr.sh_name = Integer.toString(addSectionName(".text"));
			shdr.sh_type = Elf64_Shdr.SHT_PROGBITS;
			shdr.sh_flags = Elf64_Shdr.SHF_ALLOC | Elf64_Shdr.SHF_EXECINSTR;
			shdr.sh_addr = "ELF64__SECTION_TEXT";
			shdr.sh_offset = "ELF64__SECTION_TEXT - $$";
			shdr.sh_size = "ELF64__SECTION_TEXT_SIZE";
			shdr.sh_link = "0";
			shdr.sh_info = "0";
			shdr.sh_addralign = "10h";
			shdr.sh_entsize = "0";
		}
		
		{ // .data
			Elf64_Shdr shdr = new Elf64_Shdr("DATA");
			shdr.debug_name = ".data";
			sectionHeaderList.add(shdr);
			shdr.sh_name = Integer.toString(addSectionName(".data"));
			shdr.sh_type = Elf64_Shdr.SHT_PROGBITS;
			shdr.sh_flags = Elf64_Shdr.SHF_WRITE | Elf64_Shdr.SHF_ALLOC;
			shdr.sh_addr = "ELF64__SECTION_DATA";
			shdr.sh_offset = "ELF64__SECTION_DATA - $$";
			shdr.sh_size = "ELF64__SECTION_DATA_SIZE";
			shdr.sh_link = "0";
			shdr.sh_info = "0";
			shdr.sh_addralign = "10h";
			shdr.sh_entsize = "0";
		}
		
		sectionHeaderList.addAll(debugList);
		
		{ // .shstrtab
			Elf64_Shdr shdr = new Elf64_Shdr("SHSTRTAB");
			shdr.debug_name = ".shstrtab";
			sectionHeaderList.add(shdr);
			shdr.sh_name = Integer.toString(addSectionName(".shstrtab"));
			shdr.sh_type = Elf64_Shdr.SHT_STRTAB;
			shdr.sh_flags = 0;
			shdr.sh_addr = "0";
			shdr.sh_offset = "ELF64__SECTION_SHSTRTAB - $$";
			shdr.sh_size = "ELF64__SECTION_SHSTRTAB_SIZE";
			shdr.sh_link = "0";
			shdr.sh_info = "0";
			shdr.sh_addralign = "1h";
			shdr.sh_entsize = "0";
		}
	}
	
	private void setupProgramHeaders() {
		{ // entry
			Elf64_Phdr phdr = new Elf64_Phdr();
			programHeaderList.add(phdr);
			phdr.p_type = Elf64_Phdr.PT_LOAD;
			phdr.p_flags = Elf64_Phdr.PF_R;
			phdr.p_offset = "0";
			phdr.p_vaddr = "$$";
			phdr.p_paddr = "$$";
			phdr.p_filesz = "ELF64__EHDR_PHDR_SIZE";
			phdr.p_memsz = "ELF64__EHDR_PHDR_SIZE";
			phdr.p_align = "1000h";
		}
		
		{ // .text
			Elf64_Phdr phdr = new Elf64_Phdr();
			phdr.debug_name = ".text";
			programHeaderList.add(phdr);
			phdr.p_type = Elf64_Phdr.PT_LOAD;
			phdr.p_flags = Elf64_Phdr.PF_X | Elf64_Phdr.PF_R;
			phdr.p_offset = "ELF64__SECTION_TEXT - $$";
			phdr.p_vaddr = "ELF64__SECTION_TEXT";
			phdr.p_paddr = "ELF64__SECTION_TEXT";
			phdr.p_filesz = "ELF64__SECTION_TEXT_SIZE";
			phdr.p_memsz = "ELF64__SECTION_TEXT_SIZE";
			phdr.p_align = "1000h";
		}
		
		{ // .data
			Elf64_Phdr phdr = new Elf64_Phdr();
			phdr.debug_name = ".data";
			programHeaderList.add(phdr);
			phdr.p_type = Elf64_Phdr.PT_LOAD;
			phdr.p_flags = Elf64_Phdr.PF_W | Elf64_Phdr.PF_R;
			phdr.p_offset = "ELF64__SECTION_DATA - $$";
			phdr.p_vaddr = "ELF64__SECTION_DATA";
			phdr.p_paddr = "ELF64__SECTION_DATA";
			phdr.p_filesz = "ELF64__SECTION_DATA_SIZE";
			phdr.p_memsz = "ELF64__SECTION_DATA_SIZE";
			phdr.p_align = "1000h";
		}
	}
	
	private void setupSymbols(AsmCodeGenerator.AsmContext context) {
		{ // .null
			Elf64_Sym sym = new Elf64_Sym();
			sym.debug_name = ".null";
			symtabDebug.add(sym);
			sym.st_name = Integer.toString(addSymbolName(""));
			sym.st_info = 0;
			sym.st_other = "0";
			sym.st_shndx = "0";
			sym.st_value = "0";
			sym.st_size = "0";
		}
		
		{ // .text
			Elf64_Sym sym = new Elf64_Sym();
			sym.debug_name = ".text";
			symtabDebug.add(sym);
			sym.st_name = "0";
			sym.st_info = Elf64_Sym.STT_SECTION;
			sym.st_other = "0";
			sym.st_shndx = "ELF64__SECTION_TEXT_INDEX";
			sym.st_value = "ELF64__SECTION_TEXT";
			sym.st_size = "0";
		}
		
		{ // .data
			Elf64_Sym sym = new Elf64_Sym();
			sym.debug_name = ".data";
			symtabDebug.add(sym);
			sym.st_name = "0";
			sym.st_info = Elf64_Sym.STT_SECTION;
			sym.st_other = "0";
			sym.st_shndx = "ELF64__SECTION_DATA_INDEX";
			sym.st_value = "ELF64__SECTION_DATA";
			sym.st_size = "0";
		}
		
		// Globals
		for (String item : context.globalStrings.keySet()) {
			{ // .data
				Elf64_Sym sym = new Elf64_Sym();
				sym.debug_name = ".data";
				symtabDebug.add(sym);
				sym.st_name = Integer.toString(addSymbolName(item));
				sym.st_info = 0;
				sym.st_other = "0";
				sym.st_shndx = "ELF64__SECTION_DATA_INDEX";
				sym.st_value = item;
				sym.st_size = "0";
			}
		}
		
		// Labels
		
		for (var item : context.labelStrings.entrySet()) {
			String addr = item.getKey();
			String name = item.getValue();
			
			{ // .data
				Elf64_Sym sym = new Elf64_Sym();
				sym.debug_name = ".text";
				symtabDebug.add(sym);
				sym.st_name = Integer.toString(addSymbolName(name));
				sym.st_info = 0;
				sym.st_other = "0";
				sym.st_shndx = "ELF64__SECTION_TEXT_INDEX";
				sym.st_value = addr;
				sym.st_size = "0";
			}
		}
	}
	
	public void appendHeader(StringBuilder sb) {
		// Setup section headers
		setupSectionHeaders();
		
		// Setup program headers
		setupProgramHeaders();
		
		// Setup file header
		fileHeader.e_type = Elf64_Ehdr.ET_EXEC;
		fileHeader.e_machine = Elf64_Ehdr.EM_X86_64;
		fileHeader.e_entry = "ELF64__SECTION_TEXT";
		fileHeader.e_phoff = "ELF64__PHDR - $$";
		fileHeader.e_shoff = "ELF64__SHDR - $$";
		fileHeader.e_flags = "0";
		fileHeader.e_ehsize = Integer.toString(Elf64_Ehdr.SIZE);
		fileHeader.e_phentsize = Integer.toString(Elf64_Phdr.SIZE);
		fileHeader.e_phnum = Integer.toString(programHeaderList.size());
		fileHeader.e_shentsize = Integer.toString(Elf64_Shdr.SIZE);
		fileHeader.e_shnum = Integer.toString(sectionHeaderList.size());
		fileHeader.e_shstrndx = "ELF64__SECTION_SHSTRTAB_INDEX";
		
		// Create data
		sb.append("BITS 64\n");
		sb.append("org 400000h\n\n");
		
		for (int i = 0; i < sectionHeaderList.size(); i++) {
			Elf64_Shdr item = sectionHeaderList.get(i);
			sb.append("ELF64__SECTION_").append(item.label_name).append("_INDEX equ ").append(i).append("\n");
		}
		
		sb.append("ELF64__EHDR:\n");
		fileHeader.append(sb);
		
		sb.append("ELF64__PHDR:\n");
		for (Elf64_Phdr item : programHeaderList) {
			item.append(sb);
		}
		sb.append("ELF64__EHDR_PHDR_SIZE equ $ - $$\n");
	}
	
	public void appendSectionText(StringBuilder sb, String content, String code) {
		sb.append("align 1000h,db 0\n");
		sb.append("ELF64__SECTION_TEXT:\n");
		sb.append("align 1h,db 0\n");
		sb.append(content);
		sb.append(code);
		sb.append("ELF64__SECTION_TEXT_SIZE equ $ - ELF64__SECTION_TEXT\n");
		sb.append("\n");
	}
	
	public void appendSectionData(StringBuilder sb, AsmCodeGenerator.AsmContext context) {
		sb.append("align 1000h,db 0\n");
		sb.append("ELF64__SECTION_DATA:\n");
		sb.append("align 1h,db 0\n");
		for (Map.Entry<String, byte[]> entry : context.globalStrings.entrySet()) {
			sb.append("    ").append(entry.getKey()).append(" db ");
			byte[] array = entry.getValue();
			
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				
				int c = Byte.toUnsignedInt(array[i]);
				if (Character.isLetterOrDigit(c)) {
					sb.append("'").append((char) c).append("'");
				} else {
					sb.append("0x%02x".formatted(c));
				}
			}
			
			if (array.length > 0) {
				sb.append(", ");
			}
			
			sb.append("0\n");
		}
		sb.append("ELF64__SECTION_DATA_SIZE equ $ - ELF64__SECTION_DATA\n");
		sb.append("\n");
	}
	
	public void appendSectionSections(StringBuilder sb) {
		// Section headers
		sb.append("align 10h,db 0\n");
		sb.append("ELF64__SHDR:\n");
		sb.append("align 1h,db 0\n");
		for (Elf64_Shdr item : sectionHeaderList) {
			item.append(sb);
		}
		sb.append("\n");
		
		// Section names
		sb.append("ELF64__SECTION_SHSTRTAB:\n");
		for (String item : sectionNames) {
			sb.append("     db \"").append(item).append("\", 0\n");
		}
		sb.append("ELF64__SECTION_SHSTRTAB_SIZE equ $ - ELF64__SECTION_SHSTRTAB\n");
		sb.append("\n");
	}
	
	public void appendSectionDebug(StringBuilder sb, AsmCodeGenerator.AsmContext context) {
		if (!debug) {
			return;
		}
		
		// Setup symbols
		setupSymbols(context);
		
		// .symtab
		sb.append("align 8h,db 0\n");
		sb.append("ELF64__SECTION_SYMTAB:\n");
		sb.append("align 1h,db 0\n");
		for (Elf64_Sym item : symtabDebug) {
			item.append(sb);
		}
		sb.append("ELF64__SECTION_SYMTAB_SIZE equ $ - ELF64__SECTION_SYMTAB\n");
		sb.append("\n");
		
		// .strtab
		sb.append("ELF64__SECTION_STRTAB:\n");
		for (String item : strtabDebug) {
			sb.append("     db \"").append(item).append("\", 0\n");
		}
		sb.append("ELF64__SECTION_STRTAB_SIZE equ $ - ELF64__SECTION_STRTAB\n");
		sb.append("\n");
	}
	
	public class Elf64_Ehdr {
		public static final int SIZE = 0x40;
		
		public static final String ET_NONE = "0",
			ET_REL = "1",
			ET_EXEC = "2",
			ET_DYN = "3",
			ET_CORE = "4";
		
		public static final String EM_X86 = "0x03",
			EM_X86_64 = "0x3e";
		
		public String e_type;
		public String e_machine;
		public String e_entry;
		public String e_phoff;
		public String e_shoff;
		public String e_flags;
		public String e_ehsize;
		public String e_phentsize;
		public String e_phnum;
		public String e_shentsize;
		public String e_shnum;
		public String e_shstrndx;
		
		public void append(StringBuilder sb) {
			sb.append("    db 0x7f, \"ELF\", 2, 1, 1, 0\n");
			sb.append("    db 0, 0, 0, 0, 0, 0, 0, 0\n");
			sb.append("    dw ").append(e_type).append("\n");      // e_type
			sb.append("    dw ").append(e_machine).append("\n");   // e_machine
			sb.append("    dd 1\n");                               // e_version
			sb.append("    dq ").append(e_entry).append("\n");     // e_entry
			sb.append("    dq ").append(e_phoff).append("\n");     // e_phoff
			sb.append("    dq ").append(e_shoff).append("\n");     // e_shoff
			sb.append("    dd ").append(e_flags).append("\n");     // e_flags
			sb.append("    dw ").append(e_ehsize).append("\n");    // e_ehsize
			sb.append("    dw ").append(e_phentsize).append("\n"); // e_phentsize
			sb.append("    dw ").append(e_phnum).append("\n");     // e_phnum
			sb.append("    dw ").append(e_shentsize).append("\n"); // e_shentsize
			sb.append("    dw ").append(e_shnum).append("\n");     // e_shnum
			sb.append("    dw ").append(e_shstrndx).append("\n");  // e_shstrndx
		}
	}
	
	public class Elf64_Phdr {
		public static final int SIZE = 0x38;
		
		public static final String PT_NULL = "0",
			PT_LOAD = "1",
			PT_DYNAMIC = "2",
			PT_INTERP = "3",
			PT_NOTE = "4",
			PT_SHLIB = "5",
			PT_PHDR = "6",
			PT_TLS = "7";
		
		public static final int PF_X = 1,
			PF_W = 2,
			PF_R = 4;
		
		private String debug_name;
		public String p_type;
		public int p_flags;
		public String p_offset;
		public String p_vaddr;
		public String p_paddr;
		public String p_filesz;
		public String p_memsz;
		public String p_align;
		
		public void append(StringBuilder sb) {
			if (debug_name != null) {
				sb.append("; program '").append(debug_name).append("'\n");
			}
			
			sb.append("    dd ").append(p_type).append("\n");   // p_type
			sb.append("    dd ").append(p_flags).append("\n");  // p_flags
			sb.append("    dq ").append(p_offset).append("\n"); // p_offset
			sb.append("    dq ").append(p_vaddr).append("\n");  // p_vaddr
			sb.append("    dq ").append(p_paddr).append("\n");  // p_paddr
			sb.append("    dq ").append(p_filesz).append("\n"); // p_filesz
			sb.append("    dq ").append(p_memsz).append("\n");  // p_memsz
			sb.append("    dq ").append(p_align).append("\n");  // p_align
		}
	}
	
	private class Elf64_Shdr {
		public static final int SIZE = 0x40;
		
		public static final String SHT_NULL = "0x00",
			SHT_PROGBITS = "0x01",
			SHT_SYMTAB = "0x02",
			SHT_STRTAB = "0x03",
			SHT_RELA = "0x04",
			SHT_HASH = "0x05",
			SHT_DYNAMIC = "0x06",
			SHT_NOTE = "0x07",
			SHT_NOBITS = "0x08",
			SHT_REL = "0x09",
			SHT_SHLIB = "0x0a",
			SHT_DYNSYM = "0x0b",
			SHT_INIT_ARR = "0x0e",
			SHT_FINI_ARR = "0x0f",
			SHT_PREINIT_ = "0x10",
			SHT_GROUP = "0x11",
			SHT_SYMTAB_SHNDX = "0x12",
			SHT_NUM = "0x13";
		
		public static final int SHF_WRITE = 0x1,
			SHF_ALLOC = 0x2,
			SHF_EXECINSTR = 0x4,
			SHF_MERGE = 0x10,
			SHF_STRINGS = 0x20,
			SHF_INFO_LINK = 0x40,
			SHF_LINK_ORDER = 0x80,
			SHF_OS_NONCONFORMING = 0x100,
			SHF_GROUP = 0x200,
			SHF_TLS = 0x400;
		
		private String label_name;
		private String debug_name;
		
		// Fields
		public String sh_name;
		public String sh_type;
		public int sh_flags;
		public String sh_addr;
		public String sh_offset;
		public String sh_size;
		public String sh_link;
		public String sh_info;
		public String sh_addralign;
		public String sh_entsize;
		
		public Elf64_Shdr(String label_name) {
			this.label_name = label_name;
		}
		
		public void append(StringBuilder sb) {
			if (debug_name != null) {
				sb.append("; section '").append(debug_name).append("'\n");
			}
			
			sb.append("    dd ").append(sh_name).append("\n");      // sh_name
			sb.append("    dd ").append(sh_type).append("\n");      // sh_type
			sb.append("    dq ").append(sh_flags).append("\n");     // sh_flags
			sb.append("    dq ").append(sh_addr).append("\n");      // sh_addr
			sb.append("    dq ").append(sh_offset).append("\n");    // sh_offset
			sb.append("    dq ").append(sh_size).append("\n");      // sh_size
			sb.append("    dd ").append(sh_link).append("\n");      // sh_link
			sb.append("    dd ").append(sh_info).append("\n");      // sh_info
			sb.append("    dq ").append(sh_addralign).append("\n"); // sh_addralign
			sb.append("    dq ").append(sh_entsize).append("\n");   // sh_entsize
		}
	}
	
	private class Elf64_Sym {
		public static final int SIZE = 0x18;
		
		public static final int STB_LOCAL = 0,
			STB_GLOBAL = 1 << 4,
			STB_WEAK = 2 << 4;
		
		public static final int STT_NOTYPE = 0,
			STT_OBJECT = 1,
			STT_FUNC = 2,
			STT_SECTION = 3,
			STT_FILE = 4,
			STT_COMMON = 5;
		
		public static final int STV_DEFAULT = 0,
			STV_INTERNAL = 1,
			STV_HIDDEN = 2,
			STV_PROTECTED = 3;
		
		private String debug_name;
		public String st_name;
		public int st_info;
		public String st_other;
		public String st_shndx;
		public String st_value;
		public String st_size;
		
		public void append(StringBuilder sb) {
			if (debug_name != null) {
				sb.append("; symbol '").append(debug_name).append("'\n");
			}
			
			sb.append("    dd ").append(st_name).append("\n");  // st_name
			sb.append("    db ").append(st_info).append("\n");  // st_info
			sb.append("    db ").append(st_other).append("\n"); // st_other
			sb.append("    dw ").append(st_shndx).append("\n"); // st_shndx
			sb.append("    dq ").append(st_value).append("\n"); // st_value
			sb.append("    dq ").append(st_size).append("\n");  // st_size
		}
	}
}
