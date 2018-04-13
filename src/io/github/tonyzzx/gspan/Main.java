package io.github.tonyzzx.gspan;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		int minsup = 1;
		int maxpat = Integer.MAX_VALUE;
		int minnodes = 0;
		boolean directed = false;

		Scanner sc = new Scanner(System.in);
		System.out.println("输入文件名");
		String filepath = sc.nextLine();
		System.out.println("输入频数");
		minsup = sc.nextInt();
		File readfile = new File(filepath);
		File writefile = new File(readfile.getName() + "_result");
		FileReader reader = new FileReader(readfile);
		FileWriter writer = new FileWriter(writefile);
		writer.flush();

		gSpan gSpan = new gSpan();
		gSpan.run(reader, writer, minsup, maxpat, minnodes, directed);
		
		sc.close();
	}
}
