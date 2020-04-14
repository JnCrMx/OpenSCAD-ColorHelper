package de.jcm.openscad.colorhelper;

import de.jcm.math.geo.Triangle3D;
import de.jcm.math.geo.vector.Vector3D;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

public class ColorHelper
{
	private static HashMap<String, Color> colorMap = new HashMap<>();
	
	private static int maxColor = "FINAL".length();
	
	public static String color(String color)
	{
		StringBuilder c = new StringBuilder("[" + color + "]");
		while(c.length() - 2 < maxColor)
			c.append(" ");
		return c.toString();
	}
	
	public static void printHelp()
	{
		System.out.println("java -jar ColorHelper.jar [options] [input-file]");
		System.out.println();
		
		System.out.println("Option\t\t\t\tShort\t\t\tDecription\t\t\t\t\t\tDefault");
		System.out.println(
				"--output-dir [dir]\t\t-o [dir]\t\tDirectory to output temporary files to.\t\t\tColorHelper_{input}");
		System.out.println(
				"--merge-output [file]\t\t-m [file]\t\tPattern for merged OBJ files.\t\t\t{input}_{group}.obj");
		System.out.println(
				"--mtl-output [file]\t\t--mtl [file]\t\tFinal output file for MTL file (MaterialLibrary).\t{input}.mtl");
		System.out.println(
				"--scad-pattern [pattern]\t\t\t\tPattern for SCAD output files.\t\t\t\t{number}_{input}_{group}_{color}.scad");
		System.out.println(
				"--stl-pattern [pattern]\t\t\t\t\tPattern for STL output files.\t\t\t\t{number}_{input}_{group}_{color}.stl");
		System.out.println(
				"--obj-pattern [pattern]\t\t\t\t\tPattern for OBJ output files.\t\t\t\t{number}_{input}_{group}_{color}.obj");
		System.out.println("--material-pattern [pattern]\t\t\t\tPattern for material names.\t\t\t\t{color}");
		System.out.println(
				"--exe [file]\t\t\t\t\t\tOpenSCAD executable.\t\t\t\t\tC:\\Program Files\\OpenSCAD\\openscad.exe OR C:\\Program Files (x86)\\OpenSCAD\\openscad.exe");
		System.out.println("--color-map [file]\t\t--colors\t\tFile containing color definitions.\t\t\tcolors.map");
		System.out.println("--set [key]=[value]\t\t-s\t\t\tSet a property (multiple allowed).");
		System.out.println("--help\t\t\t\t-h\t\t\tPrint this help message and exit.");
		System.out.println("--version\t\t\t-v\t\t\tPrint version and exit.");
		
		System.out.println();
		System.out.println("Variables for patterns:");
		System.out.println("\t{number}\t:\tNumber of the active color.");
		System.out.println("\t{color}\t\t:\tName of the active color.");
		System.out.println("\t{group}\t\t:\tName of the active group or \"main\".");
		System.out.println("\t{input}\t\t:\tName of the input file without file extension.");
	}
	
	public static void printVersion()
	{
		System.out.println("OpenSCAD ColorHelper v1.0 by JCM");
	}
	
	public static void main(String[] args) throws Exception
	{
		File input = null;
		File output = null;
		File mtlOutput = null;
		URL colorMapFile = ColorHelper.class.getResource("/colors.map");
		
		String outputPatternSCAD = "{number}_{input}_{group}_{color}.scad";
		String outputPatternSTL = "{number}_{input}_{group}_{color}.stl";
		String outputPatternOBJ = "{number}_{input}_{group}_{color}.obj";
		String outputPatternMerge = "{input}_{group}.obj";
		String materialPattern = "{color}";
		
		HashMap<String, String> properties = new HashMap<>();
		
		File openSCAD = new File("C:\\Program Files\\OpenSCAD\\openscad.exe");
		if(!openSCAD.exists())
			openSCAD = new File("C:\\Program Files (x86)\\OpenSCAD\\openscad.exe");
		
		Iterator<String> iterator = Arrays.asList(args).iterator();
		while(iterator.hasNext())
		{
			String arg = iterator.next();
			if(arg.equals("--output-dir") || arg.equals("-o"))
			{
				output = new File(iterator.next());
			}
			else if(arg.equals("--merge-output") || arg.equals("-m"))
			{
				outputPatternMerge = iterator.next();
			}
			else if(arg.equals("--mtl-output") || arg.equals("--mtl"))
			{
				mtlOutput = new File(iterator.next());
			}
			else if(arg.equals("--scad-pattern"))
			{
				outputPatternSCAD = iterator.next();
			}
			else if(arg.equals("--stl-pattern"))
			{
				outputPatternSTL = iterator.next();
			}
			else if(arg.equals("--obj-pattern"))
			{
				outputPatternOBJ = iterator.next();
			}
			else if(arg.equals("--material-pattern"))
			{
				materialPattern = iterator.next();
			}
			else if(arg.equals("--exe"))
			{
				openSCAD = new File(iterator.next());
			}
			else if(arg.equals("--color-map") || arg.equals("--colors"))
			{
				colorMapFile = new File(iterator.next()).toURI().toURL();
			}
			else if(arg.equals("--set") || arg.equals("-s"))
			{
				String prop = iterator.next();
				String[] o = prop.split("=");
				
				String key = o[0];
				String value = o[1];
				
				properties.putIfAbsent(key, value);
			}
			else if(arg.equals("--help") || arg.equals("-h"))
			{
				printHelp();
				System.exit(0);
			}
			else if(arg.equals("--version") || arg.equals("-v"))
			{
				printVersion();
				System.exit(0);
			}
			else if(!arg.startsWith("-"))
			{
				input = new File(arg);
			}
			else
			{
				System.out.println("Unknown argument: " + arg);
				System.out.println("Use --help for a list of arguments!");
			}
		}
		
		if(input == null)
		{
			System.out.println("No input file!");
			System.exit(2);
		}
		
		String inputName = input.getName().substring(0, input.getName().lastIndexOf('.'));
		
		if(output == null)
			output = new File(input.getParentFile(), "ColorHelper_" + inputName);
		output.mkdirs();
		
		if(mtlOutput == null)
			mtlOutput = new File(output, inputName + ".mtl");
		
		materialPattern = materialPattern.replace("{input}", inputName);
		outputPatternSCAD = outputPatternSCAD.replace("{input}", inputName);
		outputPatternSTL = outputPatternSTL.replace("{input}", inputName);
		outputPatternOBJ = outputPatternOBJ.replace("{input}", inputName);
		outputPatternMerge = outputPatternMerge.replace("{input}", inputName);
		
		DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		df.setMaximumFractionDigits(340); // 340 =
											// DecimalFormat.DOUBLE_FRACTION_DIGITS
		
		Scanner scanner = new Scanner(colorMapFile.openStream());
		
		// MARK: Read color map
		while(scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			if(!line.isEmpty() && !line.startsWith("//"))
			{
				String[] parts = line.split("=");
				String name = parts[0].trim().toLowerCase();
				String definition = parts[1].trim();
				
				if(definition.startsWith("#"))
				{
					String hex = definition.substring(1);
					if(hex.length() == 3)
					{
						String part1 = hex.substring(0, 1);
						part1 += part1;
						String part2 = hex.substring(1, 2);
						part2 += part2;
						String part3 = hex.substring(2, 3);
						part3 += part3;
						
						int r = Integer.parseInt(part1, 16);
						int g = Integer.parseInt(part2, 16);
						int b = Integer.parseInt(part3, 16);
						
						Color color = new Color(r, g, b);
						colorMap.putIfAbsent(name, color);
						
						System.out.println("Defined color: [" + name + "] = " + color.toString());
					}
					if(hex.length() == 6)
					{
						String part1 = hex.substring(0, 2);
						String part2 = hex.substring(2, 4);
						String part3 = hex.substring(4, 6);
						
						int r = Integer.parseInt(part1, 16);
						int g = Integer.parseInt(part2, 16);
						int b = Integer.parseInt(part3, 16);
						
						Color color = new Color(r, g, b);
						colorMap.putIfAbsent(name, color);
						
						System.out.println("Defined color: [" + name + "] = " + color.toString());
					}
				}
				else if(definition.startsWith("rgb("))
				{
					String sub = definition.substring(4, definition.lastIndexOf(')'));
					String[] partss = sub.split(",");
					
					int r = Integer.parseInt(partss[0].trim());
					int g = Integer.parseInt(partss[1].trim());
					int b = Integer.parseInt(partss[2].trim());
					
					Color color = new Color(r, g, b);
					colorMap.putIfAbsent(name, color);
					
					System.out.println("Defined color: [" + name + "] = " + color.toString());
				}
				else
				{
					int rgb = Integer.parseInt(definition);
					Color color = new Color(rgb);
					colorMap.putIfAbsent(name, color);
					
					System.out.println("Defined color: [" + name + "] = " + color.toString());
				}
			}
		}
		
		scanner.close();
		
		LinkedList<String> colors = new LinkedList<>();
		LinkedList<Group> groups = new LinkedList<>();
		LinkedList<String> allLines = new LinkedList<>();
		
		// MARK: Search for colors, extern variables and groups
		scanner = new Scanner(input);
		int lineNumber = 0;
		while(scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			if(line.toLowerCase().startsWith("/*start_group(\""))
			{
				int start = line.indexOf('"') + 1;
				int end = line.indexOf('"', start);
				String name = line.substring(start, end).toLowerCase();
				
				groups.add(new Group(name, lineNumber, -1));
				
				System.out.println("Found new group: [" + name + "]");
				if(name.length() > maxColor)
					maxColor = name.length();
			}
			else if(line.toLowerCase().startsWith("/*end_group(\""))
			{
				int start = line.indexOf('"') + 1;
				int end = line.indexOf('"', start);
				String name = line.substring(start, end).toLowerCase();
				
				for(Group group : groups)
				{
					if(group.getName().equals(name))
						group.setLineEnd(lineNumber);
				}
			}
			if(line.toLowerCase().startsWith("/*extern*/"))
			{
				String def = line.substring(10);
				String[] parts = def.split("=");
				
				String key = parts[0].trim();
				
				if(properties.containsKey(key))
				{
					String value = properties.get(key);
					
					allLines.add("/* prop */" + key + " = \"" + value + "\";");
					System.out.println("Set value for property " + key + " to " + value);
				}
				else
				{
					allLines.add(line);
					System.out.println("Ignored property " + key);
				}
			}
			else
			{
				allLines.add(line);
				if(line.trim().startsWith("color(\""))
				{
					int start = line.indexOf('"') + 1;
					int end = line.indexOf('"', start);
					String color = line.substring(start, end).toLowerCase();
					if(!colors.contains(color))
					{
						colors.add(color);
						System.out.println("Found new color: [" + color + "]");
						if(color.length() > maxColor)
							maxColor = color.length();
					}
				}
				else if(line.trim().startsWith("color([") || line.trim().startsWith("color( ["))
				{
					int start = line.indexOf('[') + 1;
					int end = line.indexOf(']', start);
					String c = line.substring(start, end);
					
					String[] parts = c.split(",");
					
					double r = Double.parseDouble(parts[0].trim());
					double g = Double.parseDouble(parts[1].trim());
					double b = Double.parseDouble(parts[2].trim());
					
					String color = "rgb(" + r + "," + g + "," + b + ")";
					if(!colors.contains(color))
					{
						colors.add(color);
						System.out.println("Found new color: [" + color + "]");
						if(color.length() > maxColor)
							maxColor = color.length();
					}
				}
			}
			lineNumber++;
		}
		scanner.close();
		
		groups.addFirst(new Group("main", 0, lineNumber - 1));
		
		// MARK: Build group map
		int[] groupMap = new int[lineNumber];
		for(int i = 0; i < groups.size(); i++)
		{
			Group group = groups.get(i);
			Arrays.fill(groupMap, group.getLineStart(), group.getLineEnd() + 1, i);
		}
		
		PrintStream mtl = new PrintStream(mtlOutput);

		for(Group activeGroup : groups)
		{
			LinkedList<String> lines = new LinkedList<>();

			for(int i = 0; i < allLines.size(); i++)
			{
				String line = allLines.get(i);
				if(line.trim().startsWith("color(\""))
				{
					Group group = groups.get(groupMap[i]);
					if(group == activeGroup)
						lines.add(allLines.get(i));
					else
					{
						int open = 0;
						while(true)
						{
							lines.add("//" + line);
							if(line.contains("{"))
								open++;
							if(line.contains("}"))
								open--;
							if(line.contains(";") && open <= 0)
								break;

							i++;
							line = allLines.get(i);
						}
					}
				}
				else
				{
					lines.add(line);
				}
			}
			HashMap<String, LinkedList<Triangle3D>> allTriangles = new HashMap<>();

			for(int i = 0; i < colors.size(); i++)
			{
				String activeColor = colors.get(i);
				File scad = new File(output,
				                     outputPatternSCAD.replace("{number}", Integer.toString(i))
						                     .replace("{color}", activeColor)
						                     .replace("{group}", activeGroup.getName()));
				File stl = new File(output,
				                    outputPatternSTL.replace("{number}", Integer.toString(i))
						                    .replace("{color}", activeColor).replace("{group}", activeGroup.getName()));
				File obj = new File(output,
				                    outputPatternOBJ.replace("{number}", Integer.toString(i))
						                    .replace("{color}", activeColor).replace("{group}", activeGroup.getName()));

				System.out.println(color(activeGroup.getName()) + " " + color(activeColor)
						                   + " Generating SCAD file... => " + scad.getAbsolutePath());

				FileWriter writer = new FileWriter(scad);
				for(int j = 0; j < lines.size(); j++)
				{
					String line = lines.get(j);
					if(line.trim().startsWith("color(\""))
					{
						int start = line.indexOf('"') + 1;
						int end = line.indexOf('"', start);
						String color = line.substring(start, end).toLowerCase();
						if(color.equals(activeColor))
							writer.write(line + "\n");
						else
						{
							int open = 0;
							while(true)
							{
								writer.write("//" + line + "\n");
								if(line.contains("{"))
									open++;
								if(line.contains("}"))
									open--;
								if(line.contains(";") && open <= 0)
									break;

								j++;
								line = lines.get(j);
							}
						}
					}
					else if(line.trim().startsWith("color([") || line.trim().startsWith("color( ["))
					{
						int start = line.indexOf('[') + 1;
						int end = line.indexOf(']', start);
						String c = line.substring(start, end);

						String[] parts = c.split(",");

						double r = Double.parseDouble(parts[0].trim());
						double g = Double.parseDouble(parts[1].trim());
						double b = Double.parseDouble(parts[2].trim());

						String color = "rgb(" + r + "," + g + "," + b + ")";
						if(color.equals(activeColor))
							writer.write(line + "\n");
						else
						{
							int open = 0;
							do
							{
								writer.write("//" + line + "\n");
								j++;
								line = lines.get(j);

								if(line.contains("{"))
									open++;
								if(line.contains("}"))
									open--;
							}
							while(!line.contains(";") || open != 0);
						}
					}
					else
						writer.write(line + "\n");
				}
				writer.close();

				System.out.println(color(activeGroup.getName()) + " " + color(activeColor)
						                   + " Generating STL  file... => " + stl.getAbsolutePath());
				Process proc = Runtime.getRuntime().exec("\"" + openSCAD.getAbsolutePath() + "\" -o\""
						                                         + stl.getAbsolutePath() + "\" \"" + scad
						.getAbsolutePath() + "\"");
				int exit = proc.waitFor();
				String material = materialPattern.replace("{number}", Integer.toString(i))
						.replace("{color}", activeColor);
				if(exit == 0)
				{
					System.out.println(color(activeGroup.getName()) + " " + color(activeColor)
							                   + " Generating OBJ  file... => " + obj.getAbsolutePath());

					Scanner scan = new Scanner(stl);

					LinkedList<Triangle3D> triangles = new LinkedList<>();
					String solidName = "";
					while(scan.hasNextLine())
					{
						String line = scan.nextLine();
						if(line.startsWith("solid "))
						{
							solidName = line.substring(6);
						}
						else if(line.startsWith("  facet normal "))
						{
							String n = line.substring(15);
							String[] coords = n.split(" ");

							double x = Double.parseDouble(coords[0]);
							double y = Double.parseDouble(coords[1]);
							double z = Double.parseDouble(coords[2]);

							Vector3D normal = new Vector3D(x, y, z);

							line = scan.nextLine();
							if(line.equals("    outer loop"))
							{
								String[] vertices = new String[3];
								vertices[0] = scan.nextLine();
								vertices[1] = scan.nextLine();
								vertices[2] = scan.nextLine();

								Vector3D[] points = new Vector3D[3];

								for(int j = 0; j < 3; j++)
								{
									String string = vertices[j];
									if(string.startsWith("      vertex "))
									{
										String v = string.substring(13);
										coords = v.split(" ");

										x = Double.parseDouble(coords[0]);
										y = Double.parseDouble(coords[1]);
										z = Double.parseDouble(coords[2]);

										points[j] = new Vector3D(x, y, z);
									}
								}
								triangles.add(new Triangle3D(points[0], points[1], points[2], normal));
							}
						}
					}
					scan.close();

					allTriangles.put(activeColor, triangles);

					PrintStream print = new PrintStream(obj);

					print.println("mtllib " + mtlOutput.getAbsolutePath());
					print.println("usemtl "
							              + material.replace("{group}", activeGroup.getName()));

					print.println("o " + solidName);

					for(Triangle3D triangle : triangles)
					{
						for(int j = 0; j < 3; j++)
						{
							double x = triangle.getVertex(j).getX();
							double y = triangle.getVertex(j).getY();
							double z = triangle.getVertex(j).getZ();

							print.println("v " + df.format(x) + " " + df.format(y) + " " + df.format(z));
						}
					}
					for(Triangle3D triangle : triangles)
					{
						double x = triangle.getNormal().getX();
						double y = triangle.getNormal().getY();
						double z = triangle.getNormal().getZ();

						print.println("vn " + df.format(x) + " " + df.format(y) + " " + df.format(z));
					}
					for(int j = 0; j < triangles.size(); j++)
					{
						int base = (j * 3) + 1;

						print.println("f " + base + "//" + (j + 1) + " " + (base + 1) + "//" + (j + 1) + " "
								              + (base + 2) + "//" + (j + 1));
					}

					print.close();

					System.out.println(color(activeGroup.getName()) + " " + color(activeColor)
							                   + " Generating MTL entry... => " + mtlOutput.getAbsolutePath());

					if(activeColor.startsWith("rgb("))
					{
						String sub = activeColor.substring(4, activeColor.lastIndexOf(')'));
						String[] parts = sub.split(",");

						double r = Double.parseDouble(parts[0]);
						double g = Double.parseDouble(parts[1]);
						double b = Double.parseDouble(parts[2]);

						mtl.println("newmtl " + material);
						mtl.println("Kd " + r + " " + g + " " + b);
					}
					else
					{
						Color color = colorMap.get(activeColor);

						double r = color.getRed();
						double g = color.getGreen();
						double b = color.getBlue();

						r /= 255;
						g /= 255;
						b /= 255;

						mtl.println("newmtl " + material);
						mtl.println("Kd " + df.format(r) + " " + df.format(g) + " " + df.format(b));
					}
				}
				else
				{
					System.out.println(color(activeGroup.getName()) + " " + color(activeColor)
							                   + " OpenSCAD returned exit code " + exit);

					System.out.println(color(activeGroup.getName()) + " " + color(activeColor)
							                   + " Creating   STL  file... => " + stl.getAbsolutePath());
					PrintStream print = new PrintStream(stl);

					print.println("solid " + "empty");
					print.println("endsolid " + "empty");

					print.close();

					System.out.println(color(activeGroup.getName()) + " " + color(activeColor)
							                   + " Creating   OBJ  file... => " + obj.getAbsolutePath());
					print = new PrintStream(obj);
					print.println("mtllib " + mtlOutput.getAbsolutePath());
					print.println("usemtl "
							              + material.replace("{group}", activeGroup.getName()));

					print.println("o " + "empty");
					print.close();

					allTriangles.put(activeColor, new LinkedList<>());
				}
			}

			File mergeOBJ = new File(output,
			                         outputPatternMerge.replace("{group}", activeGroup.getName()));
			System.out.println(color(activeGroup
					                         .getName()) + " " + color("FINAL") + " Merging OBJ models... => " + mergeOBJ
					.getAbsolutePath());
			PrintStream print = new PrintStream(mergeOBJ);

			print.println("mtllib " + mtlOutput.getAbsolutePath());

			for(String activeColor : colors)
			{
				LinkedList<Triangle3D> triangles = allTriangles.get(activeColor);
				for(Triangle3D triangle : triangles)
				{
					for(int j = 0; j < 3; j++)
					{
						double x = triangle.getVertex(j).getX();
						double y = triangle.getVertex(j).getY();
						double z = triangle.getVertex(j).getZ();

						print.println("v " + df.format(x) + " " + df.format(y) + " " + df.format(z));
					}
				}
				for(Triangle3D triangle : triangles)
				{
					double x = triangle.getNormal().getX();
					double y = triangle.getNormal().getY();
					double z = triangle.getNormal().getZ();

					print.println("vn " + df.format(x) + " " + df.format(y) + " " + df.format(z));
				}
			}

			int vertex = 1;
			int normal = 1;

			for(int i = 0; i < colors.size(); i++)
			{
				String activeColor = colors.get(i);

				print.println("usemtl "
						              + materialPattern.replace("{number}", Integer.toString(i))
						.replace("{color}", activeColor));

				LinkedList<Triangle3D> triangles = allTriangles.get(activeColor);
				for(int j = 0; j < triangles.size(); j++)
				{
					print.println("f " + (vertex) + "//" + (normal) + " " + (vertex + 1) + "//" + (normal) + " "
							              + (vertex + 2) + "//" + (normal));
					vertex += 3;
					normal += 1;
				}
			}
			print.close();
		}
		mtl.close();
		
		System.out.println("Cave Johnson, we're done here.");
	}
	
}
