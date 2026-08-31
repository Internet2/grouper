---
title: "Glossary en Français"
space: Grouper
pageId: 28543089
version: 21
lastUpdated: 2026-07-12T15:26:02.644Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543089/Glossary+en+Fran+ais
---

## Grouper Glossary

Les termes spécifiques à Grouper sont définis ci-dessous, de même que certains concepts. La compréhension de ces termes vous aideront dans la maitrise des possibilités offertes par Grouper.

Les termes techniques n'ont pas été traduits.

A partir de la version 1.3.0, [la terminologie utilisée dans l'interface utilisateur de Grouper](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543683/UI+Terminology) a évolué afin d'être plus facilement compréhensible par tous. La terminologie orientée pour les développeurs et administrateurs systèmes demeure inchangés.

Skip to section on naming groups and stems

> The [TIER Grouper Deployment Guide](https://spaces.at.internet2.edu/download/attachments/93651000/TI.25.1-TIERGrouperDeploymentGuide.pdf) uses terms described in this document: NIST 800-162 doc
> 
> [Guide to Attribute Based Access Control (ABAC) Definition and Considerations](http://nvlpubs.nist.gov/nistpubs/specialpublications/NIST.sp.800-162.pdf)

| **TERM** | **DEFINITION** | Au sein de l'interface utilisateur (UI) |
| --- | --- | --- |
| *Access Privileges (Privilèges sur un groupe)* | Les privilèges ci-dessous déterminent ce qu'un ***Sujet*** peut faire sur un ***Groupe*** :    - **ADMIN** - permet d'assigner des privilèges et de modifier l'ensemble des informations du groupe (ID, nom, description, membre...). - **UPDATE** **- permet de modifier les membres du groupe (inclus le privilège **VIEW**). - **READ** - permet de voir les membres d'un groupe (inclus le privilège **VIEW**). - **VIEW** - permet de voir les informations de base du groupe : l'ID, le nom, la description **mais non les membres.** - **GROUP_ATTR_READ** - permet de voir les attributs assignés au groupe. Le sujet doit aussi posséder le privilège **ATTR_READ** sur la définition d'attribut. - **GROUP_ATTR_UPDATE** - permet de modifier les attributs assigner au groupe. Le sujet doit aussi posséder le privilège **ATTR_UPDATE** sur la définition d'attribut. - **OPTIN** - donne la possibilité de s'ajouter à la liste des membres du groupe. - **OPTOUT** - donne la possibilité de se supprimer de la liste des membres du groupe. | - ADMIN : **Administrer** - UPDATE : **Modifier** - READ : **Lire** - VIEW : **Voir** - GROUP_ATTR_READ : **Lire attributs** - GROUP_ATTR_UPDATE : **Modifier attributs** - OPTIN : **Rejoindre** - OPTOUT : **Quitter** |
| ***Attribut*** | Grouper supporte deux types de catégories d'attributs :        1. Des attributs utilisés pour attacher des métadonnées à divers objets du référentiel. [Pour en savoir plus](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework) (en anglais).        2. Une chaîne de caractères monovaluée associée à un ***groupe*** ou à un ***dossier***. Par défaut, Grouper supporte les attributs suivants :    - **id** - un identifiant global unique auto-incrémenté. - **extension**- le nom relatif du groupe ou dossier au sein du dossier parent. - **name** - utilisé pour faciliter la recherche des groupes par leur nom. C'est une chaine de caractères, en lecture seule, représentant le chemin vers le groupe : *<dossier parent>:<extension>*. Cet attribut est maintenu par le système. - **displayExtension** - une forme de l'extension du groupe destinée à l'affichage. - **displayName**- utilisé pour faciliter la recherche des groupes par leur nom. C'est une chaine de caractères, en lecture seule, représentant le chemin vers le groupe : *<displayName du dossier parent>:<displayExtension>*. Cet attribut est maintenu par le système. - **description** - la description du groupe ou du dossier. | - id : **UUID** - extension : **Identifiant (ID)** - name : **ID Chemin (ID Path)** - displayExtension : **Nom** - displayName : **Chemin (Path)** |
| ***Groupe algébrique (Composite Group)***  ****** | Un **groupe** dont les **membres** sont déterminés en combinant les membres de deux autres groupes appelés **opérandes**.  Il est possible de combiner les opérandes de 3 manières :    - **union** - tous les sujets sont membres d'un des deux groupes opérandes. Pour cette opération, pas besoin d'un groupe algébrique : il suffit simplement de rajouter un groupe en tant que membre d'un autre groupe.      ex. : Groupe Z = membres du groupe X ***OU*** du groupe Y, où Z = X U Y. - **intersection** - tous les sujets doivent être membres du premier **ET** du deuxième groupe opérande.       ex. : Group Z = membres du groupe X **ET** du groupe Y, où Z = X ∩ Y. - **complément** - tous les membres du premier groupe opérande qui **NE SONT PAS** membres du deuxième groupe opérande.       e.g., Group Z = membres du groupe X ***ET NON du*** groupe Y, où Z = X - Y. |  |
| ***Membre direct*** | Un ***Sujet*** qui est listé dans la liste d'appartenance d'un ***Groupe*** est un membre direct de ce groupe. Voir aussi ***Membre indirect***. | - Sujet : **entité** |
| ***Groupe opérande   (Factor Group)*** | Un ***Groupe*** combiné (**union, intersection, complément**) avec un autre groupe opérande qui définit le résultat d'un ***Groupe algébrique.*** |  |
| ***Dossier*** | Un endroit pour organiser des éléments dans Grouper, le plus souvent des groupes. Aussi appelé ***Stem*** ou ***Naming Stem***. |  |
| ***Groupe*** | Un groupe possède l'***Appartenance*** d'une liste de ***Sujets*** ainsi que plusieurs attributs. Cette liste peut avoir aucune ou plusieurs entrées et ne peut contenir uniquement des sujets. Les attributs associés (**id**, **extension**, **description**...) sont des chaîne de caractères monovaluée.Un groupe doit-être créé au sein d'un ***Dossier***. Si un groupe est ajouté en tant que membre d'un autre groupe, il devient donc un***sous-groupe*** et les membres de ce sous-groupe deviennent membre du groupe.   Par défaut, un groupe Grouper possède :    - 6 ***Attributs***. - Une liste de ***membres***      Ce modèle peut-être étendu pour inclure des attributs additionnels. |  |
| *Mathématiques de groupes* | Désigne la combinaisons de groupes destinées à créeer un groupe basé sur les membres de ces groupes. Voir aussi***Groupe algébrique.*** |  |
| ***Membre indirect*** | Un ***Sujet*** membre d'un ***Sous-groupe*** d'un ***G******roupe***, ou membre d'un groupe opérande qui contribue à la liste des membres du groupe, est un membre indirect de ce groupe. Voir aussi ***Membre direct.*** |  |
| ***Liste*** | Une liste multivalué faisant référence à des ***Sujets***. Les ***membres directs*** sont renseignés dans la liste des membres du groupe. Les listes sont aussi utilisées pour identifier les types de privilèges des sujets : ***Privilèges sur des dossiers*** ou ***sur des groupes***. |  |
| Membre | N'importe quel **Sujet** appartenant au moins à un groupe. Aussi, un ***Membre***d'un ***Groupe*** est un sujet possédant une appartenance direct ou indirect à ce groupe. |  |
| Appartenance | Les directs, indirects ou directs et indirects membres d'un ***Groupe***. Le type d'appartenance est déterminé par le contexte ou la configuration (***Sous-groupe***, ***Groupe algébrique***...). |  |
| ***Naming Privileges / Creation Privileges   (Privilèges sur un dossier)*** | Les privilèges ci-dessous déterminent ce qu'un ***Sujet*** peut faire sur un ***Dossier*** :    - **CREATE** - permet de créer des groupes, attributs et sous-dossiers au sein du dossier. - **ADMIN** - permet de créer des groupes, attributs et sous-dossiers au sein du dossier. Permet aussi de supprimer le dossier et d'affecter des privilèges à n'importe quelle entité. - **STEM_ATTR_READ** - permet de lit les attributs assignés au dossier. A noter que le sujet doit, aussi, posséder le privilège **ATTR_READ** sur la définition de l'attribut. - **STEM_ATTR_UPDATE** - permet d'assigner des attributs au dossier. A noter que le sujet doit, aussi, posséder le privilège **ATTR_UPDATE** sur la définition de l'attribut. | - CREATE : **Créer** - ADMIN : **Administrer** - STEM_ATTR_READ : **Lire attributs** - STEM_ATTR_UPDATE : **Modifier attributs** |
| ***Naming Stem*** | A string that forms the leading part of a ***Group's*** name. By linking the ability to create groups to a specified naming stem (via the **CREATE** privilege), the possibility that different groups can be given the same name is substantially reduced, and the name of each group can be made to reflect something about the authority under which it was created.     ...see Examples below. | Stem is a UI "folder" |
| ***Stem*** | A synonym for a ***Naming Stem***. | Stem is a UI "folder" |
| ***Subgroup*** | A ***Group*** that is a ***Direct Member*** of another group. |  |
| ***Subject*** | An abstraction of any object whose ***Memberships*** are to be managed by Grouper. Most Grouper deployments will manage subjects that represent people and groups, but computers, accounts, services, or any other type of object maintained in a back-end identity store may be presented as subjects to Grouper by use of the Subject API. | Subject is a UI "Entity" |
| ***Subject Source*** | One of the configured (generally external) places where subjects (entities) can be looked up and added to groups or assigned permissions. Each source has an unchanging and unique ID. |  |
| ***Subject*** Id | This is an unchanging (generally opaque) identifier that will be stored in the Grouper database (along with subject source id) to represent each subject when it is used (e.g. added to a group or assigned permissions). This ID must be unique in the source. Note: if removing an unresolvable subject from a group, this is the only way to reference the subject. |  |
| ***Subject*** Identifier | This is an attribute of the subject which can be used to identify the subject. Note, the Subject ID should not also be a Subject Identifier. This is not used in the Grouper database to lookup users, and can change. Examples of this are: netID and EPPN. |  |
| ***Type*** | There are two distinct uses for this term in Grouper.    - **Group Type** - (deprecated in Grouper 2.2, but functionality still supported using the [Attribute Framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework).) Each ***Group*** has one or more group types associated with it. The Grouper distribution contains support for a single group type called "**base**", but sites may register additional types, together with the attributes and lists associated with them, within their Grouper installation. Doing so enables management of groups with a richer information model or a more diverse set of information models. Note, the addition of "Role" to Grouper adds a field on Groups called typeOfGroup which can be "group", "role" (group which can have permissions assigned), and "entity" (group object which cannot have members and is used for example as an application principal - **Subject Type** - the Subject API v0.2.1 that Grouper 1.0 relies on uses the notion of a subject type, such as "person", "group", or "computer", etc. |  |

## Examples

#### **Step 1: Create a Root Naming Stem**

In the example below, a root naming stem is first created. Note: creating a naming stem is required prior to the creation of any groups.

***Naming Stem*** **uofc**

| ***attribute*** | ***value*** |
| --- | --- |
| *naming stem* | empty |
| *extension* | uofc |
| *displayExtension* | The University Of Chicago |
| *name* | uofc |
| *displayName* | The University Of Chicago |

#### **Step 2: Create a Group**

Next, a group may be created using the "uofc" naming stem.

***Group*** **uofc:exec_council**

| ***attribute*** | ***value*** |
| --- | --- |
| *naming stem* | uofc |
| *extension* | exec_council |
| *displayExtension* | Executive Council |
| *name* | uofc:exec_council |
| *displayName* | The University of Chicago:Executive Council |

#### **Step 3: Create a Subordinate Naming Stem and Group**

Name and displayName values propagate down through subordinate naming stems, e.g the Biological Sciences Division within U of C:

***Naming Stem*** **uofc:bsd**

| ***attribute*** | ***value*** |
| --- | --- |
| *naming stem* | uofc |
| *extension* | bsd |
| *displayExtension* | Biological Sciences Division |
| *name* | uofc:bsd |
| *displayName* | The University Of Chicago:Biological Sciences Division |

Again, a group is created, e.g., the Enterprise Information Systems staff, with the above naming stem, and is displayed as follows:

***Group*** **uofc:bsd:eis_staff**

| ***attribute*** | ***value*** |
| --- | --- |
| *naming stem* | uofc:bsd |
| *extension* | eis_staff |
| *displayExtension* | Enterprise Information Systems staff |
| *name* | uofc:bsd:eis_staff |
| *displayName* | The University Of Chicago:Biological Sciences Division:Enterprise Information Systems staff |

### See Also

Group and Folder Design Ideas
