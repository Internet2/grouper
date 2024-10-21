# Exporting grouper Confluence pages to XML and parse

1) Export the XML and unzip

You will need export access for the relevant Confluence space(s)

https://confluence.atlassian.com/doc/export-content-to-word-pdf-html-and-xml-139475.html

> To export pages to XML:
> 
> Go to the space and select Space tools > Content Tools from the bottom of the sidebar
> 
> Select Back up – an XML export contains every page, blog posts, comment, and attachment in the space, but excludes blog posts.
> 
> Under Save to restore directory, you can:
> - Give your XML export a file name (optional)
> - Select Save permanently if you want to keep your file on the server, otherwise, all space backups are only temporarily saved to your <home-directory>/restore/space, where <home-directory> is shared-home-directory for Confluence Data Center. You'll need access to the server to retrieve the file this way.
> 
> Select Back up
> 
> You can download the exported XML backup by selecting the file name when the export process has finished.

Make sure you are specifically in the Grouper space to do the export. If you select Custom pages to export, you should see the correct list of pages for the Grouper space, and not the
internal developer space, etc.

2) Convert bad characters

There are a few pages with string corruption; they are essentially zero-width spaces (\u200b), but not aligned to character boundaries correctly, so doing character replacement doesn't clean them up.

Just run the parser script, and it will fail but tell you the first line number with the bad character. From there you can open the xml, go to that line, and some editors like VS Code will highlight the bad character. Then remove that character, plus any others of the same character, and save it.


3) Convert to graphviz dot files

`python Python/parseConfluenceXml.py entities.xml`

This will create two files in the current directory, entities.dot and legend.dot

4) Convert to SVG

You will need the graphviz software for the `dot` command.

```
dot -Tsvg -o entities.svg entities.dot
dot -Tsvg -o legend.svg legend.dot
```

5) Open the SVG files in a web browser

Each node is labeled with its page id, and the tooltip is the page title. To get to the Wiki page referred to by its page id, you can use the URL https://spaces.at.internet2.edu/pages/viewinfo.action?pageId={pageId}.
