package newell.util;

import java.io.*;

public class CSharpToJava {

	/**
	 * @param args <C# input file> <Java input file
	 */
	public static void main(String[] args) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
			PrintWriter writer = new PrintWriter(new File(args[1]));
			String line;
			boolean previousLineWasCommentBlock = false;
			while ((line = reader.readLine()) != null) {

				if (line.startsWith("{")) continue;
				if (line.startsWith("}")) continue;
				line = line.trim();
				if (line.startsWith("namespace")) continue;
				if (line.startsWith("using")) continue; 

				if (line.startsWith("//")) {
					// Process comments.

					if (line.startsWith("///") && previousLineWasCommentBlock == false) {
						line = line.replaceAll("/// <summary>", "  /**\n   * ");
						line = line.replaceAll("///", "  /**");
						previousLineWasCommentBlock = true;
					}

					// Ignore lines.
					//if (line.equals("/// <remarks>")) continue;
					//if (line.equals("/// </remarks>")) continue;

					// Process comment block.
					line = line.replaceAll("/// <summary>", "  /**\n   * ");
					line = line.replaceAll("</summary>", ".");
					line = line.replaceAll("/// <remarks>", "   * ");
					line = line.replaceAll("</remarks>", "");
					line = line.replaceAll("/// <value>", "   * @return ");
					line = line.replaceAll("</value>", "");        
					line = line.replaceAll("/// <param name=\"", "   * @param ");
					line = line.replaceAll("\">", " ");
					line = line.replaceAll("</param>", "");
					line = line.replaceAll("/// <returns>", "   * @return ");
					line = line.replaceAll("</returns>", "");
					line = line.replaceAll("///", "   *");
					line = line.replaceAll("/// <inheritdoc>", "/** {@inheritDoc} */");

				} else {
					// End comment block.
					if (previousLineWasCommentBlock == true)
						line = "   */\n" + line;
					previousLineWasCommentBlock = false;

					// Process language constructs.
					line = line.replaceAll("override ", "");
					line = line.replaceAll("virtual ", "");
					line = line.replaceAll("\\.Count", ".size()");
					line = line.replaceAll("ICollection", "Collection");
					line = line.replaceAll(" List", " Vector");
					line = line.replaceAll("IList", "List");
					line = line.replaceAll("ISet", "Set");
					line = line.replaceAll("bool", "boolean");
					line = line.replaceAll(" : I", " implements I");
					line = line.replaceAll(" : ", " extends ");
					line = line.replaceAll("string", "String");
					line = line.replaceAll(" \\{ get; \\}", "();");
					line = line.replaceAll("Console\\.Out\\.WriteLine",   "System.out.println");
					line = line.replaceAll("Console\\.Out\\.Write",       "System.out.print");
					line = line.replaceAll("Console\\.Error\\.WriteLine", "System.err.println");
					line = line.replaceAll("Console\\.Error\\.Write",     "System.err.print");
					line = line.replaceAll(".userMatrix[user_id]", ".getUserMatrix().getRow(user_id)");
					line = line.replaceAll(".itemMatrix[item_id]", ".getItemMatrix().getRow(item_id)");
					line = line.replaceAll(" in ", " : ");
					line = line.replaceAll("Dictionary", "HashMap");
					line = line.replaceAll("<int", "<Integer");
					line = line.replaceAll("double>", "Double>");
					line = line.replaceAll("foreach", "for");
					line = line.replaceAll(" in ", " : ");
					line = line.replaceAll("static public", "public static");
					line = line.replaceAll("StreamReader", "BufferedReader");
					line = line.replaceAll("StreamWriter", "PrintWriter");
					line = line.replaceAll(" int\\.parse", " Integer.parseInt");
					line = line.replaceAll(" unit ", " int ");
					line = line.replaceAll("\\.Length", ".length");
					line = line.replaceAll("ArgumentException", "IllegalArgumentException");
					line = line.replaceAll(" base\\.", " super.");

					char[] chars = line.toCharArray();
					for (int i = 0; i < chars.length; i++)
						if (chars[i] == '.' && i < chars.length - 1)
							chars[i+1] = Character.toLowerCase(chars[i+1]);
					line = new String(chars);
				}

				//System.out.println(original);
				System.out.println(line);
				writer.println(line);
			}
			reader.close();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}