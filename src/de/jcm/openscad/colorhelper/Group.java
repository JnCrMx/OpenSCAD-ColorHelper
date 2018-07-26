package de.jcm.openscad.colorhelper;

public class Group
{
	private String name;
	private int lineStart;
	private int lineEnd;
	
	public Group(String name, int lineStart, int lineEnd)
	{
		this.name = name;
		this.lineStart = lineStart;
		this.lineEnd = lineEnd;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getLineStart()
	{
		return lineStart;
	}

	public void setLineStart(int lineStart)
	{
		this.lineStart = lineStart;
	}

	public int getLineEnd()
	{
		return lineEnd;
	}

	public void setLineEnd(int lineEnd)
	{
		this.lineEnd = lineEnd;
	}
}
