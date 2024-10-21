import xml.etree.ElementTree as ET
from datetime import datetime
import sys

class Page:
    page_ids = dict()
    page_children = dict()

    def __init__(self, id, name, modified_str, parent_id):
        self.id: str = id
        self.name: str = name
        # 2015-09-03 20:06:54.126
        self.modified_date: datetime = datetime.fromisoformat(modified_str)
        self.parent_id: str = parent_id

        Page.page_ids[self.id] = self
        self.median_modified_date: datetime = None

    def __repr__(self):
        return f"Page[id='{self.id}', name='{self.name}', modified='{self.modified_date}', parent_id = '{self.parent_id}'"

    @classmethod
    def fill_children(cls):
        for page in Page.page_ids.values():
            parent_id = page.parent_id
            if parent_id not in Page.page_children:
                Page.page_children[parent_id] = list()
            else:
                Page.page_children[parent_id].append(page.id)


def element_find(object, xpath):
    if object is None:
        return None
    return object.find(xpath)


def element_text(object, xpath):
    if object is None:
        return None
    element = object.find(xpath)
    if element is None:
        return None
    return element.text


# Open the XML file for reading
filename = sys.argv[1]

tree = ET.parse(filename)

# There are a few unicode errors in the xml. Read the file content manually and handle encoding errors
# with open(filename, 'r', encoding='utf-8', errors='replace') as f:
#     xml_content = f.read()
# tree = ET.fromstring(xml_content)

root = tree.getroot()

page_parents = dict()

# Find all <object class="Page"> elements
page_objects = root.findall(".//object[@class='Page']")

# Loop through each <object class="Page"> element
idx = 1
for page_object in page_objects:
    # Find the <property name="title"> subnode

    # id_obj = page_object.find(".//id[@name='id']")
    id = element_text(page_object, ".//id[@name='id']")
    title = element_text(page_object, ".//property[@name='title']")
    content_status = element_text(page_object, ".//property[@name='contentStatus']")
    original_version_obj = element_find(page_object, ".//property[@name='originalVersion']")
    mod_date = element_text(page_object, ".//property[@name='lastModificationDate']")
    parent_page_id = element_text(element_find(page_object, ".//property[@name='parent'][@class='Page']"),
                                  ".//id[@name='id']")

    # Check if the <property name="title"> subnode exists
    if id is not None and title is not None and original_version_obj is None:
        if content_status in {"deleted", "draft"}:
            print(f"Ignoring {content_status} page: {id} {title}")
        else:
            page_parents[id] = parent_page_id

            # Extract and print the value of the <property name="title"> subnode
            # print(f'{idx}\t{id}\t{title}\t{mod_date}\t{parent_page_id}')
            page = Page(id, title, mod_date, parent_page_id)
            # print(page)
            idx += 1

Page.fill_children()





# Print graphviz

# https://graphviz.org/gallery/

node_colors_by_year = {
    0: '#c34a36',     # red
    2008: '#845ec2',  # purple
    2011: '#d65db1',  # violet
    2015: '#ff6f91',  # rose
    2017: '#ff9671',  # tangerine
    2020: '#ffc75f',  # goldenrod
    2022: '#f9f871',  # yellow
    2023: '#fefedf',  # bone
}


# gallery demo "Module Dependencies" https://graphviz.org/Gallery/neato/softmaint.html
def print_neato(file):
    file.write("""
digraph "" {
    fontname="Helvetica,Arial,sans-serif";
    node [shape=circle,height=.12, width=.25, height=.375, fontsize=9];
    layout=neato;
    scale=4;
    center="";
    """)

    for page in Page.page_ids.values():
        name2 = page.name.replace('"', '\\"')
        mod_year = page.modified_date.year
        index_year = max([k for (k, v) in node_colors_by_year.items() if mod_year >= k])
        color = node_colors_by_year[index_year]
        file.write(f'    {page.id} [fillcolor="{color}", style="filled", tooltip="{name2}"];\n')

    for page in Page.page_ids.values():
        if page.parent_id is not None:
            file.write(f'{page.parent_id} -> {page.id};\n')

    file.write('}\n')

def print_legend(file):
    file.write(f'''
digraph cluster1 {{
    label = "Legend" ;
    shape=rectangle ;
    color = black ;
    fontsize = 20;

    a [shape=rect; style="filled"; fillcolor="{node_colors_by_year[0]}"; label="pre-2008"] ;
    b [shape=rect; style="filled"; fillcolor="{node_colors_by_year[2008]}"; label="2008"] ;
    c [shape=rect; style="filled"; fillcolor="{node_colors_by_year[2011]}"; label="2011"] ;
    d [shape=rect; style="filled"; fillcolor="{node_colors_by_year[2015]}"; label="2015"] ;
    e [shape=rect; style="filled"; fillcolor="{node_colors_by_year[2017]}"; label="2017"] ;
    f [shape=rect; style="filled"; fillcolor="{node_colors_by_year[2020]}"; label="2020"] ;
    g [shape=rect; style="filled"; fillcolor="{node_colors_by_year[2022]}"; label="2022"] ;
    h [shape=rect; style="filled"; fillcolor="{node_colors_by_year[2023]}"; label="2023"] ;
}}
    ''')


with open("entities.dot", "w") as file:
    print_neato(file)

with open("legend.dot", "w") as file:
    print_legend(file)
