/**
 * Copyright 2019 Internet2
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 *   Loads a Grouper database with sample data suitable for use in visualization testing. Data includes:
 *
 *     * 100 random registry subjects (enable the jdbc data source)
 *     * 62 folders, 157 groups, and ~500 memberships. A graph of the root node has ~250 objects, including
 *           the built-in etc folders and groups
 *     * 4 different loader jobs for basis and ref groups
 *     * two groups provisioned to PSPNG target "testDB2"
 *     * a complement group
 *     * an intersection group
 *     * a group with limited permissions in certain areas. Assign users to basis:dept:110100:staff, and
 *          log on as them to see what they should see.
 *
 *   The basis:dept:110100:staff has read permission on a few groups and view on a few others. You should note when
 *   testing as a user in this group that nodes will be linked as long as there is a chain of read permissions. The
 *   chain is broken when there is no privilege, or the privilege is only view. For example, visualization should show
 *   for a user in this group when logged into the UI:
 *
 *     * self as subject-> can see direct group since has read, only subobjects that have read (this is the same as the subject view in the UI)
 *     * app:its:pcavoid:sources: can see parents if READ but not VIEW
 *     * app:its:lbsimply:sources: all are view, can only see object itself with no members
 *
 *     This script can be executed two ways. From the command line:
 *       `gsh "/path/to/loadGraphSampleData.groovy"
 *
 *     or from a command within gsh, invoking a groovyshell evaluation:
 *       `def ret = new GroovyShell(binding).evaluate(new File("/path/to/loadGraphSampleData.groovy"))`
 *
 */

// imports needed when executing from a file evaluation
import edu.internet2.middleware.grouper.*
import edu.internet2.middleware.grouper.attr.*
import edu.internet2.middleware.grouper.group.CompositeSave;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder

// this import is needed even when executing directly in the shell
import edu.internet2.middleware.grouper.subj.cache.SubjectSourceCache

/* globals -- it's very hard to use globals in groovyshell, but a map is a workaround */
stats = [
  gshTotalObjectCount: 0L,
  gshTotalChangeCount: 0L,
  gshTotalErrorCount: 0L,
]


/* Create registry subjects, with attributes; e.g.:
 *  id: 8000000xx
 *  name: Hallie Stewart
 *  loginid: hstewart
 *  description: Hallie Stewart, 800000000
 *  email: 800000000@somewhere.someSchool.edu
 */
def loadSampleSubjects(grouperSession) {
  def subjectData = [
          [id:'800000000', loginid: 'hstewart', name: 'Hallie Stewart'],
          [id:'800000001', loginid: 'jbarker', name: 'Jameson Barker'],
          [id:'800000002', loginid: 'isutton', name: 'Issac Sutton'],
          [id:'800000003', loginid: 'mjohnston', name: 'Morgan Johnston'],
          [id:'800000004', loginid: 'ckelly', name: 'Ciara Kelly'],
          [id:'800000005', loginid: 'bwheeler', name: 'Braylon Wheeler'],
          [id:'800000006', loginid: 'kbeltran', name: 'Kaiden Beltran'],
          [id:'800000007', loginid: 'egallegos', name: 'Erika Gallegos'],
          [id:'800000008', loginid: 'tyates', name: 'Tristan Yates'],
          [id:'800000009', loginid: 'aware', name: 'Ayanna Ware'],
          [id:'800000010', loginid: 'sho', name: 'Shayla Ho'],
          [id:'800000011', loginid: 'astafford', name: 'Alfredo Stafford'],
          [id:'800000012', loginid: 'mhaynes', name: 'Miracle Haynes'],
          [id:'800000013', loginid: 'jlutz', name: 'Jason Lutz'],
          [id:'800000014', loginid: 'lwebb', name: 'Leyla Webb'],
          [id:'800000015', loginid: 'jvillarre', name: 'Julissa Villarreal'],
          [id:'800000016', loginid: 'tgrimes', name: 'Tyrese Grimes'],
          [id:'800000017', loginid: 'lnguyen', name: 'Layton Nguyen'],
          [id:'800000018', loginid: 'moneal', name: 'Moriah Oneal'],
          [id:'800000019', loginid: 'abailey', name: 'Ariel Bailey'],
          [id:'800000020', loginid: 'jwood', name: 'Jaelynn Wood'],
          [id:'800000021', loginid: 'rwaters', name: 'Rigoberto Waters'],
          [id:'800000022', loginid: 'rewing', name: 'Rayne Ewing'],
          [id:'800000023', loginid: 'sbranch', name: 'Samara Branch'],
          [id:'800000024', loginid: 'gwalter', name: 'Gaven Walter'],
          [id:'800000025', loginid: 'kdixon', name: 'Kaylah Dixon'],
          [id:'800000026', loginid: 'clucero', name: 'Charlize Lucero'],
          [id:'800000027', loginid: 'khale', name: 'Kyan Hale'],
          [id:'800000028', loginid: 'kduke', name: 'Kade Duke'],
          [id:'800000029', loginid: 'oboyer', name: 'Odin Boyer'],
          [id:'800000030', loginid: 'bkaufman', name: 'Brenden Kaufman'],
          [id:'800000031', loginid: 'emays', name: 'Easton Mays'],
          [id:'800000032', loginid: 'sgraham', name: 'Santino Graham'],
          [id:'800000033', loginid: 'lconley', name: 'Lilian Conley'],
          [id:'800000034', loginid: 'qowen', name: 'Quinn Owen'],
          [id:'800000035', loginid: 'kbenitez', name: 'Kailyn Benitez'],
          [id:'800000036', loginid: 'msolis', name: 'Milagros Solis'],
          [id:'800000037', loginid: 'bvincent', name: 'Brooklyn Vincent'],
          [id:'800000038', loginid: 'eblake', name: 'Essence Blake'],
          [id:'800000039', loginid: 'shurley', name: 'Skylar Hurley'],
          [id:'800000040', loginid: 'ktapia', name: 'Keira Tapia'],
          [id:'800000041', loginid: 'zgreer', name: 'Zaid Greer'],
          [id:'800000042', loginid: 'kcampos', name: 'Katherine Campos'],
          [id:'800000043', loginid: 'kliu', name: 'Kaiden Liu'],
          [id:'800000044', loginid: 'kgillespi', name: 'Karen Gillespie'],
          [id:'800000045', loginid: 'msmall', name: 'Mckinley Small'],
          [id:'800000046', loginid: 'wleon', name: 'William Leon'],
          [id:'800000047', loginid: 'rjordan', name: 'Robert Jordan'],
          [id:'800000048', loginid: 'ihaney', name: 'Izaiah Haney'],
          [id:'800000049', loginid: 'narroyo', name: 'Nikolai Arroyo'],
          [id:'800000050', loginid: 'asingh', name: 'Ashtyn Singh'],
          [id:'800000051', loginid: 'agrant', name: 'Addyson Grant'],
          [id:'800000052', loginid: 'gboyle', name: 'Gary Boyle'],
          [id:'800000053', loginid: 'bfoley', name: 'Braylon Foley'],
          [id:'800000054', loginid: 'dheath', name: 'Dillan Heath'],
          [id:'800000055', loginid: 'jwoodard', name: 'Joyce Woodard'],
          [id:'800000056', loginid: 'jmarquez', name: 'Jasmin Marquez'],
          [id:'800000057', loginid: 'jcampos', name: 'Jordan Campos'],
          [id:'800000058', loginid: 'kmontes', name: 'Kelvin Montes'],
          [id:'800000059', loginid: 'hlynch', name: 'Hailee Lynch'],
          [id:'800000060', loginid: 'dfloyd', name: 'Damion Floyd'],
          [id:'800000061', loginid: 'amendoza', name: 'Alexzander Mendoza'],
          [id:'800000062', loginid: 'rlucero', name: 'Reagan Lucero'],
          [id:'800000063', loginid: 'ckline', name: 'Cynthia Kline'],
          [id:'800000064', loginid: 'hho', name: 'Harper Ho'],
          [id:'800000065', loginid: 'dware', name: 'Darion Ware'],
          [id:'800000066', loginid: 'cmccarthy', name: 'Clarence Mccarthy'],
          [id:'800000067', loginid: 'nharding', name: 'Nathen Harding'],
          [id:'800000068', loginid: 'jjennings', name: 'Johnathan Jennings'],
          [id:'800000069', loginid: 'kkramer', name: 'Keon Kramer'],
          [id:'800000070', loginid: 'amitchell', name: 'Arely Mitchell'],
          [id:'800000071', loginid: 'ncopeland', name: 'Nora Copeland'],
          [id:'800000072', loginid: 'lnunez', name: 'London Nunez'],
          [id:'800000073', loginid: 'mpaul', name: 'Marcos Paul'],
          [id:'800000074', loginid: 'mdoyle', name: 'Maxim Doyle'],
          [id:'800000075', loginid: 'hhaynes', name: 'Hana Haynes'],
          [id:'800000076', loginid: 'vstevens', name: 'Van Stevens'],
          [id:'800000077', loginid: 'bsantana', name: 'Beckham Santana'],
          [id:'800000078', loginid: 'kdennis', name: 'Kael Dennis'],
          [id:'800000079', loginid: 'iclark', name: 'Irene Clark'],
          [id:'800000080', loginid: 'kmcconnel', name: 'Kiana Mcconnell'],
          [id:'800000081', loginid: 'kmolina', name: 'Kaylah Molina'],
          [id:'800000082', loginid: 'cboyle', name: 'Carolyn Boyle'],
          [id:'800000083', loginid: 'dsaunders', name: 'Dario Saunders'],
          [id:'800000084', loginid: 'hnorris', name: 'Heaven Norris'],
          [id:'800000085', loginid: 'cweiss', name: 'Cheyenne Weiss'],
          [id:'800000086', loginid: 'nmueller', name: 'Nathen Mueller'],
          [id:'800000087', loginid: 'cbarton', name: 'Conrad Barton'],
          [id:'800000088', loginid: 'schristia', name: 'Salvador Christian'],
          [id:'800000089', loginid: 'chicks', name: 'Cecelia Hicks'],
          [id:'800000090', loginid: 'dmccormic', name: 'Devin Mccormick'],
          [id:'800000091', loginid: 'jho', name: 'Jaida Ho'],
          [id:'800000092', loginid: 'rmoss', name: 'Ramon Moss'],
          [id:'800000093', loginid: 'ktorres', name: 'Kenyon Torres'],
          [id:'800000094', loginid: 'igomez', name: 'Isabelle Gomez'],
          [id:'800000095', loginid: 'kmoyer', name: 'Kadyn Moyer'],
          [id:'800000096', loginid: 'sberger', name: 'Savion Berger'],
          [id:'800000097', loginid: 'asullivan', name: 'Annie Sullivan'],
          [id:'800000098', loginid: 'wmullen', name: 'Warren Mullen'],
          [id:'800000099', loginid: 'kmcmillan', name: 'Kianna Mcmillan'],
  ]

  long gshTotalSubjectCount = 0L
  long gshTotalAddedCount = 0L

  SubjectSourceCache.clearCache()

  subjectData.each { row ->
    Subject subject = SubjectFinder.findById(row.id, false)

    if (subject == null) {
      subject = RegistrySubject.add(s=grouperSession,
                          id=row.id,
                          type="person",
                          name=row.name,
                          nameAttributeValue=row.name,
                          loginid=row.loginid,
                          description="${row.name}, ${row.id}",
                          email="${row.id}@somewhere.someSchool.edu")
      gshTotalAddedCount++
    }
    gshTotalSubjectCount++
  }

  println "${new Date()} Done with registry subjects, total managed: ${gshTotalSubjectCount}, expected total: 100, added: ${gshTotalAddedCount}"

  SubjectSourceCache.clearCache()
}

def loadSampleFolders(grouperSession, stats) {
  def stemData = [
          [name: 'app', displayName: 'Applications'],
          [name: 'app:its', displayName: 'Applications:ITS'],
          [name: 'app:its:cttoday', displayName: 'Applications:ITS:Citizen Throw Today'],
          [name: 'app:its:cttoday:etc', displayName: 'Applications:ITS:Citizen Throw Today:etc'],
          [name: 'app:its:lbsimply', displayName: 'Applications:ITS:Lot Buy Simply'],
          [name: 'app:its:lbsimply:etc', displayName: 'Applications:ITS:Lot Buy Simply:etc'],
          [name: 'app:its:nistaff', displayName: 'Applications:ITS:National Including Staff'],
          [name: 'app:its:nistaff:etc', displayName: 'Applications:ITS:National Including Staff:etc'],
          [name: 'app:its:nsreturn', displayName: 'Applications:ITS:Next Student Return'],
          [name: 'app:its:nsreturn:etc', displayName: 'Applications:ITS:Next Student Return:etc'],
          [name: 'app:its:pcavoid', displayName: 'Applications:ITS:Performance Clearly Avoid'],
          [name: 'app:its:pcavoid:etc', displayName: 'Applications:ITS:Performance Clearly Avoid:etc'],
          [name: 'basis', displayName: 'basis'],
          [name: 'basis:dept', displayName: 'basis:dept'],
          [name: 'basis:dept:100000', displayName: 'basis:dept:100000 - Decentralized Holistic Ability'],
          [name: 'basis:dept:110000', displayName: 'basis:dept:110000'],
          [name: 'basis:dept:110100', displayName: 'basis:dept:110100'],
          [name: 'basis:dept:110101', displayName: 'basis:dept:110101'],
          [name: 'basis:dept:110102', displayName: 'basis:dept:110102'],
          [name: 'basis:dept:110103', displayName: 'basis:dept:110103'],
          [name: 'basis:dept:110200', displayName: 'basis:dept:110200'],
          [name: 'basis:dept:110201', displayName: 'basis:dept:110201'],
          [name: 'basis:dept:110202', displayName: 'basis:dept:110202'],
          [name: 'basis:dept:110203', displayName: 'basis:dept:110203'],
          [name: 'basis:dept:120000', displayName: 'basis:dept:120000'],
          [name: 'basis:dept:120100', displayName: 'basis:dept:120100'],
          [name: 'basis:dept:120101', displayName: 'basis:dept:120101'],
          [name: 'basis:dept:120102', displayName: 'basis:dept:120102'],
          [name: 'basis:dept:120103', displayName: 'basis:dept:120103'],
          [name: 'basis:dept:120200', displayName: 'basis:dept:120200'],
          [name: 'basis:dept:120201', displayName: 'basis:dept:120201'],
          [name: 'basis:dept:120202', displayName: 'basis:dept:120202'],
          [name: 'basis:dept:120203', displayName: 'basis:dept:120203'],
          [name: 'basis:dept:130000', displayName: 'basis:dept:130000'],
          [name: 'basis:dept:130100', displayName: 'basis:dept:130100'],
          [name: 'basis:dept:130101', displayName: 'basis:dept:130101'],
          [name: 'basis:dept:130102', displayName: 'basis:dept:130102'],
          [name: 'basis:dept:130103', displayName: 'basis:dept:130103'],
          [name: 'basis:dept:130200', displayName: 'basis:dept:130200'],
          [name: 'basis:dept:130201', displayName: 'basis:dept:130201'],
          [name: 'basis:dept:130202', displayName: 'basis:dept:130202'],
          [name: 'basis:dept:130203', displayName: 'basis:dept:130203'],
          [name: 'basis:org', displayName: 'basis:Organizations'],
          [name: 'basis:org:100000', displayName: 'basis:Organizations:100000 - Decentralized Holistic Ability'],
          [name: 'basis:org:110000', displayName: 'basis:Organizations:110000 - Streamlined Composite Neural-Net'],
          [name: 'basis:org:110100', displayName: 'basis:Organizations:110100 - Adaptive Asymmetric Solution'],
          [name: 'basis:org:110200', displayName: 'basis:Organizations:110200 - Profit-Focused Reciprocal Firmware'],
          [name: 'basis:org:120000', displayName: 'basis:Organizations:120000 - Sharable Asynchronous Local Area Network'],
          [name: 'basis:org:120100', displayName: 'basis:Organizations:120100 - Integrated Empowering Interface'],
          [name: 'basis:org:120200', displayName: 'basis:Organizations:120200 - Grass-Roots Client-Server Support'],
          [name: 'basis:org:130000', displayName: 'basis:Organizations:130000 - Reduced Incremental Firmware'],
          [name: 'basis:org:130100', displayName: 'basis:Organizations:130100 - Visionary Maximized Access'],
          [name: 'basis:org:130200', displayName: 'basis:Organizations:130200 - Open-Architected Clear-Thinking Infrastructure'],
          [name: 'ref', displayName: 'Reference'],
          [name: 'ref:job', displayName: 'Reference:Jobs'],
          [name: 'ref:job:by-category', displayName: 'Reference:Jobs:By Category'],
          [name: 'ref:job:by-role', displayName: 'Reference:Jobs:By Role'],
          [name: 'etc:attribute:hooks', description: 'folder for hooks settings', displayName: 'etc:attribute:hooks'],
          [name: 'etc:loader', displayName: 'etc:loader'],
          [name: 'etc:loader:basis', displayName: 'etc:loader:basis'],
          [name: 'etc:loader:ref', displayName: 'etc:loader:ref'],
          [name: 'etc:pspng', description: 'Location for pspng-management objects.', displayName: 'etc:pspng'],
  ]

  stemData.each { row ->
    StemSave stemSave = new StemSave(grouperSession).assignName(row.name).assignCreateParentStemsIfNotExist(true).assignDisplayName(row.displayName).assignDescription(row.description)
    stem = stemSave.save()
    stats.gshTotalObjectCount++
    if (stemSave.getSaveResultType() != SaveResultType.NO_CHANGE) {
      println "Made change for stem: ${stem.name}"
      stats.gshTotalChangeCount++
    }
  }

  println "${new Date()} Done with folders, objects: ${stats.gshTotalObjectCount}, expected approx total: 62, changes: ${stats.gshTotalChangeCount}, known errors (view output for full list): ${stats.gshTotalErrorCount}"
}

def loadSampleGroups(grouperSession, stats) {
  def groupData = [
    [name: 'app:its:cttoday:etc:readers', displayName: 'Applications:ITS:Citizen Throw Today:etc:Citizen Throw Today Readers'],
    [name: 'app:its:cttoday:etc:updaters', displayName: 'Applications:ITS:Citizen Throw Today:etc:Citizen Throw Today Updaters'],
    [name: 'app:its:cttoday:sources', displayName: 'Applications:ITS:Citizen Throw Today:Citizen Throw Today Sources'],
    [name: 'app:its:lbsimply:authorized', displayName: 'Applications:ITS:Lot Buy Simply:Lot Buy Simply Authorized'],
    [name: 'app:its:lbsimply:deny', displayName: 'Applications:ITS:Lot Buy Simply:Lot Buy Simply Deny'],
    [name: 'app:its:lbsimply:etc:readers', displayName: 'Applications:ITS:Lot Buy Simply:etc:Lot Buy Simply Readers'],
    [name: 'app:its:lbsimply:etc:updaters', displayName: 'Applications:ITS:Lot Buy Simply:etc:Lot Buy Simply Updaters'],
    [name: 'app:its:lbsimply:sources', displayName: 'Applications:ITS:Lot Buy Simply:Lot Buy Simply Sources'],
    [name: 'app:its:nistaff:etc:readers', displayName: 'Applications:ITS:National Including Staff:etc:National Including Staff Readers'],
    [name: 'app:its:nistaff:etc:updaters', displayName: 'Applications:ITS:National Including Staff:etc:National Including Staff Updaters'],
    [name: 'app:its:nistaff:sources', displayName: 'Applications:ITS:National Including Staff:National Including Staff Sources'],
    [name: 'app:its:nsreturn:etc:readers', displayName: 'Applications:ITS:Next Student Return:etc:Next Student Return Readers'],
    [name: 'app:its:nsreturn:etc:updaters', displayName: 'Applications:ITS:Next Student Return:etc:Next Student Return Updaters'],
    [name: 'app:its:nsreturn:filter', displayName: 'Applications:ITS:Next Student Return:Next Student Return Filter'],
    [name: 'app:its:nsreturn:Next Student Return Authorized', displayName: 'Applications:ITS:Next Student Return:Next Student Return Authorized'],
    [name: 'app:its:nsreturn:sources', displayName: 'Applications:ITS:Next Student Return:Next Student Return Sources'],
    [name: 'app:its:pcavoid:etc:readers', displayName: 'Applications:ITS:Performance Clearly Avoid:etc:Performance Clearly Avoid Readers'],
    [name: 'app:its:pcavoid:etc:updaters', displayName: 'Applications:ITS:Performance Clearly Avoid:etc:Performance Clearly Avoid Updaters'],
    [name: 'app:its:pcavoid:sources', displayName: 'Applications:ITS:Performance Clearly Avoid:Performance Clearly Avoid Sources'],
    [name: 'basis:dept:100000:affiliate', description: 'Affiliate - Decentralized Holistic Ability auto-created by grouperLoader', displayName: 'basis:dept:100000 - Decentralized Holistic Ability:Affiliate - Decentralized Holistic Ability'],
    [name: 'basis:dept:100000:contractor', description: 'Contractor - Decentralized Holistic Ability auto-created by grouperLoader', displayName: 'basis:dept:100000 - Decentralized Holistic Ability:Contractor - Decentralized Holistic Ability'],
    [name: 'basis:dept:100000:staff', description: 'Staff - Decentralized Holistic Ability auto-created by grouperLoader', displayName: 'basis:dept:100000 - Decentralized Holistic Ability:Staff - Decentralized Holistic Ability'],
    [name: 'basis:dept:110000:affiliate', description: 'Affiliate - Streamlined Composite Neural-Net auto-created by grouperLoader', displayName: 'basis:dept:110000:Affiliate - Streamlined Composite Neural-Net'],
    [name: 'basis:dept:110000:staff', description: 'Staff - Streamlined Composite Neural-Net auto-created by grouperLoader', displayName: 'basis:dept:110000:Staff - Streamlined Composite Neural-Net'],
    [name: 'basis:dept:110000:student_worker', description: 'Student Worker - Streamlined Composite Neural-Net auto-created by grouperLoader', displayName: 'basis:dept:110000:Student Worker - Streamlined Composite Neural-Net'],
    [name: 'basis:dept:110100:affiliate', description: 'Affiliate - Adaptive Asymmetric Solution auto-created by grouperLoader', displayName: 'basis:dept:110100:Affiliate - Adaptive Asymmetric Solution'],
    [name: 'basis:dept:110100:staff', description: 'Staff - Adaptive Asymmetric Solution auto-created by grouperLoader', displayName: 'basis:dept:110100:Staff - Adaptive Asymmetric Solution'],
    [name: 'basis:dept:110101:contractor', description: 'Contractor - AAS - Innovative Radical Success auto-created by grouperLoader', displayName: 'basis:dept:110101:Contractor - AAS - Innovative Radical Success'],
    [name: 'basis:dept:110101:staff', description: 'Staff - AAS - Innovative Radical Success auto-created by grouperLoader', displayName: 'basis:dept:110101:Staff - AAS - Innovative Radical Success'],
    [name: 'basis:dept:110102:contractor', description: 'Contractor - AAS - Ergonomic Hybrid Open System auto-created by grouperLoader', displayName: 'basis:dept:110102:Contractor - AAS - Ergonomic Hybrid Open System'],
    [name: 'basis:dept:110102:staff', description: 'Staff - AAS - Ergonomic Hybrid Open System auto-created by grouperLoader', displayName: 'basis:dept:110102:Staff - AAS - Ergonomic Hybrid Open System'],
    [name: 'basis:dept:110103:faculty', description: 'Faculty - AAS - Inverse System-Worthy Superstructure auto-created by grouperLoader', displayName: 'basis:dept:110103:Faculty - AAS - Inverse System-Worthy Superstructure'],
    [name: 'basis:dept:110103:staff', description: 'Staff - AAS - Inverse System-Worthy Superstructure auto-created by grouperLoader', displayName: 'basis:dept:110103:Staff - AAS - Inverse System-Worthy Superstructure'],
    [name: 'basis:dept:110200:affiliate', description: 'Affiliate - Profit-Focused Reciprocal Firmware auto-created by grouperLoader', displayName: 'basis:dept:110200:Affiliate - Profit-Focused Reciprocal Firmware'],
    [name: 'basis:dept:110200:faculty', description: 'Faculty - Profit-Focused Reciprocal Firmware auto-created by grouperLoader', displayName: 'basis:dept:110200:Faculty - Profit-Focused Reciprocal Firmware'],
    [name: 'basis:dept:110200:staff', description: 'Staff - Profit-Focused Reciprocal Firmware auto-created by grouperLoader', displayName: 'basis:dept:110200:Staff - Profit-Focused Reciprocal Firmware'],
    [name: 'basis:dept:110201:faculty', description: 'Faculty - PRF - Multi-Tiered Intangible Software auto-created by grouperLoader', displayName: 'basis:dept:110201:Faculty - PRF - Multi-Tiered Intangible Software'],
    [name: 'basis:dept:110201:staff', description: 'Staff - PRF - Multi-Tiered Intangible Software auto-created by grouperLoader', displayName: 'basis:dept:110201:Staff - PRF - Multi-Tiered Intangible Software'],
    [name: 'basis:dept:110202:affiliate', description: 'Affiliate - PRF - Integrated Motivating Frame auto-created by grouperLoader', displayName: 'basis:dept:110202:Affiliate - PRF - Integrated Motivating Frame'],
    [name: 'basis:dept:110202:faculty', description: 'Faculty - PRF - Integrated \"test\' Motivating Frame auto-created by grouperLoader <', displayName: 'basis:dept:110202:Faculty - PRF - Integrated \"test\' <Motivating Frame'],
    [name: 'basis:dept:110202:staff', description: 'Staff - PRF - Integrated Motivating Frame auto-created by grouperLoader', displayName: 'basis:dept:110202:Staff - PRF - Integrated Motivating Frame'],
    [name: 'basis:dept:110203:contractor', description: 'Contractor - PRF - Fully-Configurable Even-Keeled Throughput auto-created by grouperLoader', displayName: 'basis:dept:110203:Contractor - PRF - Fully-Configurable Even-Keeled Throughput'],
    [name: 'basis:dept:110203:staff', description: 'Staff - PRF - Fully-Configurable Even-Keeled Throughput auto-created by grouperLoader', displayName: 'basis:dept:110203:Staff - PRF - Fully-Configurable Even-Keeled Throughput'],
    [name: 'basis:dept:120000:staff', description: 'Staff - Sharable Asynchronous Local Area Network auto-created by grouperLoader', displayName: 'basis:dept:120000:Staff - Sharable Asynchronous Local Area Network'],
    [name: 'basis:dept:120000:student_worker', description: 'Student Worker - Sharable Asynchronous Local Area Network auto-created by grouperLoader', displayName: 'basis:dept:120000:Student Worker - Sharable Asynchronous Local Area Network'],
    [name: 'basis:dept:120100:contractor', description: 'Contractor - Integrated Empowering Interface auto-created by grouperLoader', displayName: 'basis:dept:120100:Contractor - Integrated Empowering Interface'],
    [name: 'basis:dept:120100:faculty', description: 'Faculty - Integrated Empowering Interface auto-created by grouperLoader', displayName: 'basis:dept:120100:Faculty - Integrated Empowering Interface'],
    [name: 'basis:dept:120100:staff', description: 'Staff - Integrated Empowering Interface auto-created by grouperLoader', displayName: 'basis:dept:120100:Staff - Integrated Empowering Interface'],
    [name: 'basis:dept:120101:faculty', description: 'Faculty - IEI - Polarized 24/7 Capacity auto-created by grouperLoader', displayName: 'basis:dept:120101:Faculty - IEI - Polarized 24/7 Capacity'],
    [name: 'basis:dept:120101:staff', description: 'Staff - IEI - Polarized 24/7 Capacity auto-created by grouperLoader', displayName: 'basis:dept:120101:Staff - IEI - Polarized 24/7 Capacity'],
    [name: 'basis:dept:120102:faculty', description: 'Faculty - IEI - Managed Radical Artificial Intelligence auto-created by grouperLoader', displayName: 'basis:dept:120102:Faculty - IEI - Managed Radical Artificial Intelligence'],
    [name: 'basis:dept:120102:staff', description: 'Staff - IEI - Managed Radical Artificial Intelligence auto-created by grouperLoader', displayName: 'basis:dept:120102:Staff - IEI - Managed Radical Artificial Intelligence'],
    [name: 'basis:dept:120103:faculty', description: 'Faculty - IEI - Centralized Cohesive Approach auto-created by grouperLoader', displayName: 'basis:dept:120103:Faculty - IEI - Centralized Cohesive Approach'],
    [name: 'basis:dept:120103:staff', description: 'Staff - IEI - Centralized Cohesive Approach auto-created by grouperLoader', displayName: 'basis:dept:120103:Staff - IEI - Centralized Cohesive Approach'],
    [name: 'basis:dept:120103:student_worker', description: 'Student Worker - IEI - Centralized Cohesive Approach auto-created by grouperLoader', displayName: 'basis:dept:120103:Student Worker - IEI - Centralized Cohesive Approach'],
    [name: 'basis:dept:120200:contractor', description: 'Contractor - Grass-Roots Client-Server Support auto-created by grouperLoader', displayName: 'basis:dept:120200:Contractor - Grass-Roots Client-Server Support'],
    [name: 'basis:dept:120200:faculty', description: 'Faculty - Grass-Roots Client-Server Support auto-created by grouperLoader', displayName: 'basis:dept:120200:Faculty - Grass-Roots Client-Server Support'],
    [name: 'basis:dept:120200:staff', description: 'Staff - Grass-Roots Client-Server Support auto-created by grouperLoader', displayName: 'basis:dept:120200:Staff - Grass-Roots Client-Server Support'],
    [name: 'basis:dept:120201:affiliate', description: 'Affiliate - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries auto-created by grouperLoader', displayName: 'basis:dept:120201:Affiliate - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries'],
    [name: 'basis:dept:120201:faculty', description: 'Faculty - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries auto-created by grouperLoader', displayName: 'basis:dept:120201:Faculty - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries'],
    [name: 'basis:dept:120201:staff', description: 'Staff - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries auto-created by grouperLoader', displayName: 'basis:dept:120201:Staff - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries'],
    [name: 'basis:dept:120201:student_worker', description: 'Student Worker - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries auto-created by grouperLoader', displayName: 'basis:dept:120201:Student Worker - GCS - Vision-Oriented Attitude-Oriented Info-Mediaries'],
    [name: 'basis:dept:120202:contractor', description: 'Contractor - GCS - Object-Based Human-Resource Frame auto-created by grouperLoader', displayName: 'basis:dept:120202:Contractor - GCS - Object-Based Human-Resource Frame'],
    [name: 'basis:dept:120202:faculty', description: 'Faculty - GCS - Object-Based Human-Resource Frame auto-created by grouperLoader', displayName: 'basis:dept:120202:Faculty - GCS - Object-Based Human-Resource Frame'],
    [name: 'basis:dept:120202:staff', description: 'Staff - GCS - Object-Based Human-Resource Frame auto-created by grouperLoader', displayName: 'basis:dept:120202:Staff - GCS - Object-Based Human-Resource Frame'],
    [name: 'basis:dept:120203:contractor', description: 'Contractor - GCS - Centralized Value-Added Access auto-created by grouperLoader', displayName: 'basis:dept:120203:Contractor - GCS - Centralized Value-Added Access'],
    [name: 'basis:dept:120203:staff', description: 'Staff - GCS - Centralized Value-Added Access auto-created by grouperLoader', displayName: 'basis:dept:120203:Staff - GCS - Centralized Value-Added Access'],
    [name: 'basis:dept:130000:contractor', description: 'Contractor - Reduced Incremental Firmware auto-created by grouperLoader', displayName: 'basis:dept:130000:Contractor - Reduced Incremental Firmware'],
    [name: 'basis:dept:130000:staff', description: 'Staff - Reduced Incremental Firmware auto-created by grouperLoader', displayName: 'basis:dept:130000:Staff - Reduced Incremental Firmware'],
    [name: 'basis:dept:130100:contractor', description: 'Contractor - Visionary Maximized Access auto-created by grouperLoader', displayName: 'basis:dept:130100:Contractor - Visionary Maximized Access'],
    [name: 'basis:dept:130100:faculty', description: 'Faculty - Visionary Maximized Access auto-created by grouperLoader', displayName: 'basis:dept:130100:Faculty - Visionary Maximized Access'],
    [name: 'basis:dept:130100:staff', description: 'Staff - Visionary Maximized Access auto-created by grouperLoader', displayName: 'basis:dept:130100:Staff - Visionary Maximized Access'],
    [name: 'basis:dept:130101:affiliate', description: 'Affiliate - VMA - Inverse Incremental Hardware auto-created by grouperLoader', displayName: 'basis:dept:130101:Affiliate - VMA - Inverse Incremental Hardware'],
    [name: 'basis:dept:130101:staff', description: 'Staff - VMA - Inverse Incremental Hardware auto-created by grouperLoader', displayName: 'basis:dept:130101:Staff - VMA - Inverse Incremental Hardware'],
    [name: 'basis:dept:130101:student_worker', description: 'Student Worker - VMA - Inverse Incremental Hardware auto-created by grouperLoader', displayName: 'basis:dept:130101:Student Worker - VMA - Inverse Incremental Hardware'],
    [name: 'basis:dept:130102:staff', description: 'Staff - VMA - Polarized Homogeneous Task-Force auto-created by grouperLoader', displayName: 'basis:dept:130102:Staff - VMA - Polarized Homogeneous Task-Force'],
    [name: 'basis:dept:130103:contractor', description: 'Contractor - VMA - Digitized Logistical Alliance auto-created by grouperLoader', displayName: 'basis:dept:130103:Contractor - VMA - Digitized Logistical Alliance'],
    [name: 'basis:dept:130103:faculty', description: 'Faculty - VMA - Digitized Logistical Alliance auto-created by grouperLoader', displayName: 'basis:dept:130103:Faculty - VMA - Digitized Logistical Alliance'],
    [name: 'basis:dept:130103:staff', description: 'Staff - VMA - Digitized Logistical Alliance auto-created by grouperLoader', displayName: 'basis:dept:130103:Staff - VMA - Digitized Logistical Alliance'],
    [name: 'basis:dept:130200:affiliate', description: 'Affiliate - Open-Architected Clear-Thinking Infrastructure auto-created by grouperLoader', displayName: 'basis:dept:130200:Affiliate - Open-Architected Clear-Thinking Infrastructure'],
    [name: 'basis:dept:130200:contractor', description: 'Contractor - Open-Architected Clear-Thinking Infrastructure auto-created by grouperLoader', displayName: 'basis:dept:130200:Contractor - Open-Architected Clear-Thinking Infrastructure'],
    [name: 'basis:dept:130200:faculty', description: 'Faculty - Open-Architected Clear-Thinking Infrastructure auto-created by grouperLoader', displayName: 'basis:dept:130200:Faculty - Open-Architected Clear-Thinking Infrastructure'],
    [name: 'basis:dept:130200:staff', description: 'Staff - Open-Architected Clear-Thinking Infrastructure auto-created by grouperLoader', displayName: 'basis:dept:130200:Staff - Open-Architected Clear-Thinking Infrastructure'],
    [name: 'basis:dept:130201:affiliate', description: 'Affiliate - OCI - Expanded 3Rdgeneration Open Architecture auto-created by grouperLoader', displayName: 'basis:dept:130201:Affiliate - OCI - Expanded 3Rdgeneration Open Architecture'],
    [name: 'basis:dept:130201:staff', description: 'Staff - OCI - Expanded 3Rdgeneration Open Architecture auto-created by grouperLoader', displayName: 'basis:dept:130201:Staff - OCI - Expanded 3Rdgeneration Open Architecture'],
    [name: 'basis:dept:130201:student_worker', description: 'Student Worker - OCI - Expanded 3Rdgeneration Open Architecture auto-created by grouperLoader', displayName: 'basis:dept:130201:Student Worker - OCI - Expanded 3Rdgeneration Open Architecture'],
    [name: 'basis:dept:130202:affiliate', description: 'Affiliate - OCI - Integrated Multimedia Model auto-created by grouperLoader', displayName: 'basis:dept:130202:Affiliate - OCI - Integrated Multimedia Model'],
    [name: 'basis:dept:130202:faculty', description: 'Faculty - OCI - Integrated Multimedia Model auto-created by grouperLoader', displayName: 'basis:dept:130202:Faculty - OCI - Integrated Multimedia Model'],
    [name: 'basis:dept:130202:staff', description: 'Staff - OCI - Integrated Multimedia Model auto-created by grouperLoader', displayName: 'basis:dept:130202:Staff - OCI - Integrated Multimedia Model'],
    [name: 'basis:dept:130203:affiliate', description: 'Affiliate - OCI - Automated National Database auto-created by grouperLoader', displayName: 'basis:dept:130203:Affiliate - OCI - Automated National Database'],
    [name: 'basis:dept:130203:contractor', description: 'Contractor - OCI - Automated National Database auto-created by grouperLoader', displayName: 'basis:dept:130203:Contractor - OCI - Automated National Database'],
    [name: 'basis:dept:130203:staff', description: 'Staff - OCI - Automated National Database auto-created by grouperLoader', displayName: 'basis:dept:130203:Staff - OCI - Automated National Database'],
    [name: 'basis:org:100000:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:100000 - Decentralized Holistic Ability:Affiliate'],
    [name: 'basis:org:100000:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:100000 - Decentralized Holistic Ability:Contractor'],
    [name: 'basis:org:100000:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:100000 - Decentralized Holistic Ability:Employee'],
    [name: 'basis:org:100000:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:100000 - Decentralized Holistic Ability:Staff'],
    [name: 'basis:org:110000:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:110000 - Streamlined Composite Neural-Net:Affiliate'],
    [name: 'basis:org:110000:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:110000 - Streamlined Composite Neural-Net:Employee'],
    [name: 'basis:org:110000:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:110000 - Streamlined Composite Neural-Net:Staff'],
    [name: 'basis:org:110000:student_worker', description: 'Student Worker auto-created by grouperLoader', displayName: 'basis:Organizations:110000 - Streamlined Composite Neural-Net:Student Worker'],
    [name: 'basis:org:110100:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:110100 - Adaptive Asymmetric Solution:Affiliate'],
    [name: 'basis:org:110100:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:110100 - Adaptive Asymmetric Solution:Contractor'],
    [name: 'basis:org:110100:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:110100 - Adaptive Asymmetric Solution:Employee'],
    [name: 'basis:org:110100:faculty', description: 'Faculty auto-created by grouperLoader', displayName: 'basis:Organizations:110100 - Adaptive Asymmetric Solution:Faculty'],
    [name: 'basis:org:110100:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:110100 - Adaptive Asymmetric Solution:Staff'],
    [name: 'basis:org:110200:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:110200 - Profit-Focused Reciprocal Firmware:Affiliate'],
    [name: 'basis:org:110200:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:110200 - Profit-Focused Reciprocal Firmware:Contractor'],
    [name: 'basis:org:110200:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:110200 - Profit-Focused Reciprocal Firmware:Employee'],
    [name: 'basis:org:110200:faculty', description: 'Faculty auto-created by grouperLoader', displayName: 'basis:Organizations:110200 - Profit-Focused Reciprocal Firmware:Faculty'],
    [name: 'basis:org:110200:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:110200 - Profit-Focused Reciprocal Firmware:Staff'],
    [name: 'basis:org:120000:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:120000 - Sharable Asynchronous Local Area Network:Affiliate'],
    [name: 'basis:org:120000:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:120000 - Sharable Asynchronous Local Area Network:Employee'],
    [name: 'basis:org:120000:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:120000 - Sharable Asynchronous Local Area Network:Staff'],
    [name: 'basis:org:120000:student_worker', description: 'Student Worker auto-created by grouperLoader', displayName: 'basis:Organizations:120000 - Sharable Asynchronous Local Area Network:Student Worker'],
    [name: 'basis:org:120100:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:120100 - Integrated Empowering Interface:Affiliate'],
    [name: 'basis:org:120100:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:120100 - Integrated Empowering Interface:Contractor'],
    [name: 'basis:org:120100:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:120100 - Integrated Empowering Interface:Employee'],
    [name: 'basis:org:120100:faculty', description: 'Faculty auto-created by grouperLoader', displayName: 'basis:Organizations:120100 - Integrated Empowering Interface:Faculty'],
    [name: 'basis:org:120100:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:120100 - Integrated Empowering Interface:Staff'],
    [name: 'basis:org:120100:student_worker', description: 'Student Worker auto-created by grouperLoader', displayName: 'basis:Organizations:120100 - Integrated Empowering Interface:Student Worker'],
    [name: 'basis:org:120200:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:120200 - Grass-Roots Client-Server Support:Affiliate'],
    [name: 'basis:org:120200:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:120200 - Grass-Roots Client-Server Support:Contractor'],
    [name: 'basis:org:120200:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:120200 - Grass-Roots Client-Server Support:Employee'],
    [name: 'basis:org:120200:faculty', description: 'Faculty auto-created by grouperLoader', displayName: 'basis:Organizations:120200 - Grass-Roots Client-Server Support:Faculty'],
    [name: 'basis:org:120200:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:120200 - Grass-Roots Client-Server Support:Staff'],
    [name: 'basis:org:120200:student_worker', description: 'Student Worker auto-created by grouperLoader', displayName: 'basis:Organizations:120200 - Grass-Roots Client-Server Support:Student Worker'],
    [name: 'basis:org:130000:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:130000 - Reduced Incremental Firmware:Affiliate'],
    [name: 'basis:org:130000:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:130000 - Reduced Incremental Firmware:Contractor'],
    [name: 'basis:org:130000:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:130000 - Reduced Incremental Firmware:Employee'],
    [name: 'basis:org:130000:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:130000 - Reduced Incremental Firmware:Staff'],
    [name: 'basis:org:130100:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:130100 - Visionary Maximized Access:Affiliate'],
    [name: 'basis:org:130100:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:130100 - Visionary Maximized Access:Contractor'],
    [name: 'basis:org:130100:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:130100 - Visionary Maximized Access:Employee'],
    [name: 'basis:org:130100:faculty', description: 'Faculty auto-created by grouperLoader', displayName: 'basis:Organizations:130100 - Visionary Maximized Access:Faculty'],
    [name: 'basis:org:130100:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:130100 - Visionary Maximized Access:Staff'],
    [name: 'basis:org:130100:student_worker', description: 'Student Worker auto-created by grouperLoader', displayName: 'basis:Organizations:130100 - Visionary Maximized Access:Student Worker'],
    [name: 'basis:org:130200:affiliate', description: 'Affiliate auto-created by grouperLoader', displayName: 'basis:Organizations:130200 - Open-Architected Clear-Thinking Infrastructure:Affiliate'],
    [name: 'basis:org:130200:contractor', description: 'Contractor auto-created by grouperLoader', displayName: 'basis:Organizations:130200 - Open-Architected Clear-Thinking Infrastructure:Contractor'],
    [name: 'basis:org:130200:employee', description: 'Employee auto-created by grouperLoader', displayName: 'basis:Organizations:130200 - Open-Architected Clear-Thinking Infrastructure:Employee'],
    [name: 'basis:org:130200:faculty', description: 'Faculty auto-created by grouperLoader', displayName: 'basis:Organizations:130200 - Open-Architected Clear-Thinking Infrastructure:Faculty'],
    [name: 'basis:org:130200:staff', description: 'Staff auto-created by grouperLoader', displayName: 'basis:Organizations:130200 - Open-Architected Clear-Thinking Infrastructure:Staff'],
    [name: 'basis:org:130200:student_worker', description: 'Student Worker auto-created by grouperLoader', displayName: 'basis:Organizations:130200 - Open-Architected Clear-Thinking Infrastructure:Student Worker'],
    [name: 'etc:loader:basis:depts', displayName: 'etc:loader:basis:Basis Dept loader'],
    [name: 'etc:loader:basis:orgs', displayName: 'etc:loader:basis:Basis Organization loader'],
    [name: 'etc:loader:ref:job-categories', displayName: 'etc:loader:ref:Ref Jobs by Category loader'],
    [name: 'etc:loader:ref:job-roles', displayName: 'etc:loader:ref:Ref Jobs by Role loader'],
    [name: 'ref:job:by-category:all-affiliate', description: 'All Affiliate auto-created by grouperLoader', displayName: 'Reference:Jobs:By Category:All Affiliate'],
    [name: 'ref:job:by-category:all-contractor', description: 'All Contractor auto-created by grouperLoader', displayName: 'Reference:Jobs:By Category:All Contractor'],
    [name: 'ref:job:by-category:all-employee', description: 'All Employee auto-created by grouperLoader', displayName: 'Reference:Jobs:By Category:All Employee'],
    [name: 'ref:job:by-category:all-faculty', description: 'All Faculty auto-created by grouperLoader', displayName: 'Reference:Jobs:By Category:All Faculty'],
    [name: 'ref:job:by-category:all-staff', description: 'All Staff auto-created by grouperLoader', displayName: 'Reference:Jobs:By Category:All Staff'],
    [name: 'ref:job:by-category:all-student_worker', description: 'All Student_Worker auto-created by grouperLoader', displayName: 'Reference:Jobs:By Category:All Student_Worker'],
    [name: 'ref:job:by-role:all-affiliate', description: 'All Affiliate auto-created by grouperLoader', displayName: 'Reference:Jobs:By Role:All Affiliate'],
    [name: 'ref:job:by-role:all-contractor', description: 'All Contractor auto-created by grouperLoader', displayName: 'Reference:Jobs:By Role:All Contractor'],
    [name: 'ref:job:by-role:all-faculty', description: 'All Faculty auto-created by grouperLoader', displayName: 'Reference:Jobs:By Role:All Faculty'],
    [name: 'ref:job:by-role:all-staff', description: 'All Staff auto-created by grouperLoader', displayName: 'Reference:Jobs:By Role:All Staff'],
    [name: 'ref:job:by-role:all-student_worker', description: 'All Student_Worker auto-created by grouperLoader', displayName: 'Reference:Jobs:By Role:All Student_Worker'],
  ]

  groupData.each { row ->
    groupSave = new GroupSave(grouperSession).assignName(row.name).assignCreateParentStemsIfNotExist(true).assignDisplayName(row.displayName).assignDescription(row.description).assignTypeOfGroup(TypeOfGroup.group)
    group = groupSave.save()
    stats.gshTotalObjectCount++
    if (groupSave.getSaveResultType() != SaveResultType.NO_CHANGE) {
      println "Made change for group: ${group.name}"
      stats.gshTotalChangeCount++
    }
  }

  println "${new Date()} Done with groups, objects: ${stats.gshTotalObjectCount}, expected approx total: 219, changes: ${stats.gshTotalChangeCount}, known errors (view output for full list): ${stats.gshTotalErrorCount}"
}

def loadSampleComposites(grouperSession, stats) {
  def groupData = [
          [owner: 'app:its:lbsimply:authorized', left: 'app:its:lbsimply:sources', right: 'app:its:lbsimply:deny', type: CompositeType.COMPLEMENT],
          [owner: 'app:its:nsreturn:Next Student Return Authorized', left: 'app:its:nsreturn:sources', right: 'app:its:nsreturn:filter', type: CompositeType.INTERSECTION],
  ]

  groupData.each { row ->
    Group ownerGroup = GroupFinder.findByName(grouperSession, row.owner, false)
    Group leftFactorGroup = GroupFinder.findByName(grouperSession, row.left, false)
    Group rightFactorGroup = GroupFinder.findByName(grouperSession, row.right, false)
    CompositeType compositeType = row.type

    if (ownerGroup != null) {
      if (leftFactorGroup != null) {
        if (rightFactorGroup != null) {
          CompositeSave compositeSave = new CompositeSave(grouperSession).assignOwnerGroup(ownerGroup).assignCompositeType(compositeType).assignLeftFactorGroup(leftFactorGroup).assignRightFactorGroup(rightFactorGroup)
          stats.gshTotalObjectCount++
          Composite composite = compositeSave.save()
          if (compositeSave.getSaveResultType() != SaveResultType.NO_CHANGE) {
            println "Made change for composite: ${composite.toString()}"
            stats.gshTotalChangeCount++
          }
        } else {
          println "ERROR: cant find rightFactorGroup: '${row.right}'"
          stats.gshTotalErrorCount++
        }
      } else {
        println "ERROR: cant find leftFactorGroup: '${row.left}'"
        stats.gshTotalErrorCount++
      }
    } else {
      println "ERROR: cant find overallGroup: '${row.owner}'"
      stats.gshTotalErrorCount++
    }
  }

  println "${new Date()} Done with composites, objects: ${stats.gshTotalObjectCount}, expected approx total: 221, changes: ${stats.gshTotalChangeCount}, known errors (view output for full list): ${stats.gshTotalErrorCount}"
}

def loadSampleAttributeDefs(grouperSession, stats) {
  def attrData = [
    [def: 'etc:pspng:provision_to_def', name: 'etc:pspng:provision_to', description: 'Defines what provisioners should process a group or groups within a folder'],
    [def: 'etc:pspng:do_not_provision_to_def', name: 'etc:pspng:do_not_provision_to', description: 'Defines what provisioners should not process a group or groups within a folder. Since the default is already for provisioners to not provision any groups, this attribute is to override a provision_to attribute set on an ancestor folder. ']
  ]

  attrData.each { row ->
    AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession).assignName(row.def).assignCreateParentStemsIfNotExist(true).assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.type).assignMultiAssignable(true).assignMultiValued(false).assignValueType(AttributeDefValueType.string)
    attributeDef = attributeDefSave.save()
    stats.gshTotalObjectCount++
    if (attributeDefSave.getSaveResultType() != SaveResultType.NO_CHANGE) {
      println "Made change for attributeDef: ${attributeDef.name}"
      stats.gshTotalChangeCount++
    }

    int changeCount = attributeDef.getAttributeDefActionDelegate().configureActionList("assign")
    stats.gshTotalObjectCount+=1
    if (changeCount > 0) {
      stats.gshTotalChangeCount += changeCount
      println "Made ${changeCount} changes for actionList of attributeDef: ${attributeDef.name}"
    }

    if (attributeDef != null) {
      AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(grouperSession, attributeDef).assignName(row.name).assignCreateParentStemsIfNotExist(true).assignDescription(row.description).assignDisplayName(row.displayName)
      attributeDefName = attributeDefNameSave.save()
      stats.gshTotalObjectCount++
      if (attributeDefNameSave.getSaveResultType() != SaveResultType.NO_CHANGE) {
        stats.gshTotalChangeCount++
        println "Made change for attributeDefName: ${attributeDefName.name}"
      }
    } else {
      stats.gshTotalErrorCount++
      println "ERROR: cant find attributeDef: '${row.name}'"
    }
  }
  println "${new Date()} Done with attribute definitions, objects: ${stats.gshTotalObjectCount}, expected approx total: 227, changes: ${stats.gshTotalChangeCount}, known errors (view output for full list): ${stats.gshTotalErrorCount}"
}

def loadSampleMembershipsAndPrivileges(grouperSession, stats) {
  def memberData = [
          [identifier: 'basis:org:110200:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:org:130000:affiliate', source: 'g:gsa', group: 'app:its:cttoday:etc:updaters'],
          [identifier: 'basis:org:130000:affiliate', source: 'g:gsa', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:org:130000:affiliate', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:130000:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:org:130200:contractor', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:130200:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'basis:org:120200:student_worker', source: 'g:gsa', group: 'app:its:cttoday:etc:updaters'],
          [identifier: 'basis:org:120200:student_worker', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:120200:student_worker', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:120200:student_worker', source: 'g:gsa', group: 'ref:job:by-category:all-student_worker'],
          [identifier: 'basis:org:100000:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'app:its:pcavoid:etc:readers', source: 'g:gsa', privileges: 'readers', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:110200:faculty', source: 'g:gsa', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:org:110200:faculty', source: 'g:gsa', group: 'ref:job:by-category:all-faculty'],
          [identifier: 'basis:org:110100:affiliate', source: 'g:gsa', group: 'app:its:nsreturn:etc:updaters'],
          [identifier: 'basis:org:110100:affiliate', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:110100:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:dept:110200:staff', source: 'g:gsa', group: 'basis:org:110200:employee'],
          [identifier: 'basis:dept:110200:staff', source: 'g:gsa', group: 'basis:org:110200:staff'],
          [identifier: 'basis:dept:110200:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:120100:student_worker', source: 'g:gsa', group: 'app:its:cttoday:etc:readers'],
          [identifier: 'basis:org:120100:student_worker', source: 'g:gsa', group: 'ref:job:by-category:all-student_worker'],
          [identifier: 'basis:dept:130103:staff', source: 'g:gsa', group: 'basis:org:130100:employee'],
          [identifier: 'basis:dept:130103:staff', source: 'g:gsa', group: 'basis:org:130100:staff'],
          [identifier: 'basis:dept:130103:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:110200:employee', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:110200:employee', source: 'g:gsa', group: 'app:its:lbsimply:etc:readers'],
          [identifier: 'basis:org:110200:employee', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:110200:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:dept:120201:staff', source: 'g:gsa', group: 'basis:org:120200:employee'],
          [identifier: 'basis:dept:120201:staff', source: 'g:gsa', group: 'basis:org:120200:staff'],
          [identifier: 'basis:dept:120201:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:110200:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'basis:dept:110103:staff', source: 'g:gsa', group: 'basis:org:110100:employee'],
          [identifier: 'basis:dept:110103:staff', source: 'g:gsa', group: 'basis:org:110100:staff'],
          [identifier: 'basis:dept:110103:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:130200:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:dept:110203:contractor', source: 'g:gsa', group: 'basis:org:110200:contractor'],
          [identifier: 'basis:dept:110203:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:dept:120103:staff', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120103:staff', source: 'g:gsa', group: 'basis:org:120100:staff'],
          [identifier: 'basis:dept:120103:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:120200:contractor', source: 'g:gsa', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:org:120200:contractor', source: 'g:gsa', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:120200:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'basis:org:130000:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'basis:org:130200:student_worker', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:130200:student_worker', source: 'g:gsa', group: 'ref:job:by-category:all-student_worker'],
          [identifier: 'basis:org:120100:faculty', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:120100:faculty', source: 'g:gsa', group: 'app:its:pcavoid:etc:updaters'],
          [identifier: 'basis:org:120100:faculty', source: 'g:gsa', group: 'ref:job:by-category:all-faculty'],
          [identifier: 'basis:org:110000:student_worker', source: 'g:gsa', group: 'app:its:lbsimply:etc:updaters'],
          [identifier: 'basis:org:110000:student_worker', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:110000:student_worker', source: 'g:gsa', group: 'ref:job:by-category:all-student_worker'],
          [identifier: 'basis:dept:130203:staff', source: 'g:gsa', group: 'basis:org:130200:employee'],
          [identifier: 'basis:dept:130203:staff', source: 'g:gsa', group: 'basis:org:130200:staff'],
          [identifier: 'basis:dept:130203:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'app:its:nsreturn:etc:readers', source: 'g:gsa', privileges: 'readers', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:120200:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:dept:120203:contractor', source: 'g:gsa', group: 'basis:org:120200:contractor'],
          [identifier: 'basis:dept:120203:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'app:its:pcavoid:etc:updaters', source: 'g:gsa', privileges: 'updaters', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:dept:130100:staff', source: 'g:gsa', group: 'basis:org:130100:employee'],
          [identifier: 'basis:dept:130100:staff', source: 'g:gsa', group: 'basis:org:130100:staff'],
          [identifier: 'basis:dept:130100:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'app:its:nistaff:etc:updaters', source: 'g:gsa', privileges: 'updaters', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:dept:110200:faculty', source: 'g:gsa', group: 'basis:org:110200:employee'],
          [identifier: 'basis:dept:110200:faculty', source: 'g:gsa', group: 'basis:org:110200:faculty'],
          [identifier: 'basis:dept:110200:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:org:110000:staff', source: 'g:gsa', group: 'app:its:nistaff:etc:updaters'],
          [identifier: 'basis:org:110000:staff', source: 'g:gsa', group: 'app:its:pcavoid:etc:readers'],
          [identifier: 'basis:org:110000:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:org:130100:student_worker', source: 'g:gsa', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:130100:student_worker', source: 'g:gsa', group: 'ref:job:by-category:all-student_worker'],
          [identifier: 'basis:dept:120202:contractor', source: 'g:gsa', group: 'basis:org:120200:contractor'],
          [identifier: 'basis:dept:120202:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:org:120000:student_worker', source: 'g:gsa', group: 'app:its:lbsimply:etc:readers'],
          [identifier: 'basis:org:120000:student_worker', source: 'g:gsa', group: 'ref:job:by-category:all-student_worker'],
          [identifier: 'basis:dept:110102:contractor', source: 'g:gsa', group: 'basis:org:110100:contractor'],
          [identifier: 'basis:dept:110102:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:org:130200:faculty', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:130200:faculty', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:130200:faculty', source: 'g:gsa', group: 'ref:job:by-category:all-faculty'],
          [identifier: 'basis:org:130100:affiliate', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:130100:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:dept:130102:staff', source: 'g:gsa', group: 'basis:org:130100:employee'],
          [identifier: 'basis:dept:130102:staff', source: 'g:gsa', group: 'basis:org:130100:staff'],
          [identifier: 'basis:dept:130102:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:120100:contractor', source: 'g:gsa', group: 'basis:org:120100:affiliate'],
          [identifier: 'basis:dept:120100:contractor', source: 'g:gsa', group: 'basis:org:120100:contractor'],
          [identifier: 'basis:dept:120100:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:dept:120100:faculty', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120100:faculty', source: 'g:gsa', group: 'basis:org:120100:faculty'],
          [identifier: 'basis:dept:120100:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:130101:staff', source: 'g:gsa', group: 'basis:org:130100:employee'],
          [identifier: 'basis:dept:130101:staff', source: 'g:gsa', group: 'basis:org:130100:staff'],
          [identifier: 'basis:dept:130101:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:110100:contractor', source: 'g:gsa', group: 'app:its:lbsimply:etc:updaters'],
          [identifier: 'basis:org:110100:contractor', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:110100:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'app:its:cttoday:etc:readers', source: 'g:gsa', privileges: 'readers', group: 'app:its:cttoday:sources'],
          [identifier: 'app:its:cttoday:etc:readers', source: 'g:gsa', privileges: 'readers', group: 'basis:dept:120000:student_worker'],
          [identifier: 'app:its:cttoday:etc:readers', source: 'g:gsa', privileges: 'readers', group: 'basis:org:120000:affiliate'],
          [identifier: 'basis:org:100000:employee', source: 'g:gsa', group: 'app:its:lbsimply:etc:readers'],
          [identifier: 'basis:org:100000:employee', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:100000:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:org:110000:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:dept:130101:student_worker', source: 'g:gsa', group: 'basis:org:130100:student_worker'],
          [identifier: 'basis:dept:130101:student_worker', source: 'g:gsa', group: 'ref:job:by-role:all-student_worker'],
          [identifier: 'basis:dept:110202:faculty', source: 'g:gsa', group: 'basis:org:110200:employee'],
          [identifier: 'basis:dept:110202:faculty', source: 'g:gsa', group: 'basis:org:110200:faculty'],
          [identifier: 'basis:dept:110202:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:130101:affiliate', source: 'g:gsa', group: 'basis:org:130100:affiliate'],
          [identifier: 'basis:dept:130101:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:120000:student_worker', source: 'g:gsa', group: 'basis:org:120000:affiliate'],
          [identifier: 'basis:dept:120000:student_worker', source: 'g:gsa', group: 'basis:org:120000:student_worker'],
          [identifier: 'basis:dept:120000:student_worker', source: 'g:gsa', group: 'ref:job:by-role:all-student_worker'],
          [identifier: 'basis:dept:110000:affiliate', source: 'g:gsa', group: 'basis:org:110000:affiliate'],
          [identifier: 'basis:dept:110000:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:110201:faculty', source: 'g:gsa', group: 'basis:org:110200:employee'],
          [identifier: 'basis:dept:110201:faculty', source: 'g:gsa', group: 'basis:org:110200:faculty'],
          [identifier: 'basis:dept:110201:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:120000:staff', source: 'g:gsa', group: 'basis:org:120000:employee'],
          [identifier: 'basis:dept:120000:staff', source: 'g:gsa', group: 'basis:org:120000:staff'],
          [identifier: 'basis:dept:120000:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:130201:staff', source: 'g:gsa', group: 'basis:org:130200:employee'],
          [identifier: 'basis:dept:130201:staff', source: 'g:gsa', group: 'basis:org:130200:staff'],
          [identifier: 'basis:dept:130201:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:100000:affiliate', source: 'g:gsa', group: 'basis:org:100000:affiliate'],
          [identifier: 'basis:dept:100000:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:110202:staff', source: 'g:gsa', group: 'basis:org:110200:employee'],
          [identifier: 'basis:dept:110202:staff', source: 'g:gsa', group: 'basis:org:110200:staff'],
          [identifier: 'basis:dept:110202:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:110201:staff', source: 'g:gsa', group: 'basis:org:110200:employee'],
          [identifier: 'basis:dept:110201:staff', source: 'g:gsa', group: 'basis:org:110200:staff'],
          [identifier: 'basis:dept:110201:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:120200:faculty', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:120200:faculty', source: 'g:gsa', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:120200:faculty', source: 'g:gsa', group: 'ref:job:by-category:all-faculty'],
          [identifier: 'basis:org:110100:staff', source: 'g:gsa', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:org:110100:staff', source: 'g:gsa', group: 'app:its:nsreturn:etc:updaters'],
          [identifier: 'basis:org:110100:staff', source: 'g:gsa', group: 'app:its:pcavoid:etc:updaters'],
          [identifier: 'basis:org:110100:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:dept:130201:student_worker', source: 'g:gsa', group: 'basis:org:130200:student_worker'],
          [identifier: 'basis:dept:130201:student_worker', source: 'g:gsa', group: 'ref:job:by-role:all-student_worker'],
          [identifier: 'basis:dept:130200:contractor', source: 'g:gsa', group: 'basis:org:130200:contractor'],
          [identifier: 'basis:dept:130200:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:dept:130100:faculty', source: 'g:gsa', group: 'basis:org:130100:employee'],
          [identifier: 'basis:dept:130100:faculty', source: 'g:gsa', group: 'basis:org:130100:faculty'],
          [identifier: 'basis:dept:130100:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:130202:affiliate', source: 'g:gsa', group: 'basis:org:130200:affiliate'],
          [identifier: 'basis:dept:130202:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'app:its:nsreturn:etc:updaters', source: 'g:gsa', privileges: 'updaters', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:dept:120101:faculty', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120101:faculty', source: 'g:gsa', group: 'basis:org:120100:faculty'],
          [identifier: 'basis:dept:120101:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [id: '800000000', source: 'jdbc', group: 'basis:dept:130202:affiliate'],
          [id: '800000001', source: 'jdbc', group: 'basis:dept:130100:contractor'],
          [id: '800000002', source: 'jdbc', group: 'basis:dept:110101:staff'],
          [id: '800000003', source: 'jdbc', group: 'basis:dept:110103:staff'],
          [id: '800000003', source: 'jdbc', group: 'basis:dept:110203:staff'],
          [id: '800000004', source: 'jdbc', group: 'basis:dept:110000:affiliate'],
          [id: '800000004', source: 'jdbc', group: 'basis:dept:110200:staff'],
          [id: '800000005', source: 'jdbc', group: 'basis:dept:120201:staff'],
          [id: '800000006', source: 'jdbc', group: 'basis:dept:120103:student_worker'],
          [id: '800000007', source: 'jdbc', group: 'basis:dept:110203:staff'],
          [id: '800000008', source: 'jdbc', group: 'basis:dept:110203:contractor'],
          [id: '800000008', source: 'jdbc', group: 'basis:dept:120000:staff'],
          [id: '800000009', source: 'jdbc', group: 'basis:dept:120200:faculty'],
          [id: '800000009', source: 'jdbc', group: 'basis:dept:120200:staff'],
          [id: '800000010', source: 'jdbc', group: 'basis:dept:130103:contractor'],
          [id: '800000011', source: 'jdbc', group: 'basis:dept:110202:staff'],
          [id: '800000011', source: 'jdbc', group: 'basis:dept:130203:affiliate'],
          [id: '800000012', source: 'jdbc', group: 'basis:dept:130200:faculty'],
          [id: '800000013', source: 'jdbc', group: 'basis:dept:130200:staff'],
          [id: '800000014', source: 'jdbc', group: 'basis:dept:110202:staff'],
          [id: '800000015', source: 'jdbc', group: 'basis:dept:110103:staff'],
          [id: '800000015', source: 'jdbc', group: 'basis:dept:120202:staff'],
          [id: '800000016', source: 'jdbc', group: 'basis:dept:130100:contractor'],
          [id: '800000017', source: 'jdbc', group: 'basis:dept:120201:student_worker'],
          [id: '800000018', source: 'jdbc', group: 'basis:dept:130200:staff'],
          [id: '800000019', source: 'jdbc', group: 'basis:dept:130101:staff'],
          [id: '800000020', source: 'jdbc', group: 'basis:dept:130201:student_worker'],
          [id: '800000021', source: 'jdbc', group: 'basis:dept:120202:contractor'],
          [id: '800000022', source: 'jdbc', group: 'basis:dept:120103:staff'],
          [id: '800000023', source: 'jdbc', group: 'basis:dept:120203:staff'],
          [id: '800000024', source: 'jdbc', group: 'basis:dept:130200:contractor'],
          [id: '800000025', source: 'jdbc', group: 'basis:dept:110200:staff'],
          [id: '800000025', source: 'jdbc', group: 'basis:dept:120100:staff'],
          [id: '800000025', source: 'jdbc', group: 'basis:dept:130103:staff'],
          [id: '800000026', source: 'jdbc', group: 'basis:dept:120100:staff'],
          [id: '800000027', source: 'jdbc', group: 'basis:dept:110203:staff'],
          [id: '800000028', source: 'jdbc', group: 'basis:dept:120203:staff'],
          [id: '800000029', source: 'jdbc', group: 'basis:dept:110100:affiliate'],
          [id: '800000029', source: 'jdbc', group: 'basis:dept:110102:contractor'],
          [id: '800000029', source: 'jdbc', group: 'basis:dept:130203:staff'],
          [id: '800000030', source: 'jdbc', group: 'basis:dept:120100:faculty'],
          [id: '800000031', source: 'jdbc', group: 'basis:dept:120200:staff'],
          [id: '800000032', source: 'jdbc', group: 'basis:dept:110101:staff'],
          [id: '800000033', source: 'jdbc', group: 'basis:dept:120102:faculty'],
          [id: '800000034', source: 'jdbc', group: 'basis:dept:110100:staff'],
          [id: '800000035', source: 'jdbc', group: 'basis:dept:130202:staff'],
          [id: '800000036', source: 'jdbc', group: 'basis:dept:120101:staff'],
          [id: '800000037', source: 'jdbc', group: 'basis:dept:110201:staff'],
          [id: '800000037', source: 'jdbc', group: 'basis:dept:130103:staff'],
          [id: '800000037', source: 'jdbc', group: 'basis:dept:130200:affiliate'],
          [id: '800000038', source: 'jdbc', group: 'basis:dept:120102:faculty'],
          [id: '800000039', source: 'jdbc', group: 'basis:dept:100000:staff'],
          [id: '800000040', source: 'jdbc', group: 'basis:dept:130000:staff'],
          [id: '800000041', source: 'jdbc', group: 'basis:dept:130102:staff'],
          [id: '800000042', source: 'jdbc', group: 'basis:dept:130102:staff'],
          [id: '800000043', source: 'jdbc', group: 'basis:dept:120200:contractor'],
          [id: '800000044', source: 'jdbc', group: 'basis:dept:120101:faculty'],
          [id: '800000045', source: 'jdbc', group: 'basis:dept:100000:contractor'],
          [id: '800000045', source: 'jdbc', group: 'basis:dept:130100:staff'],
          [id: '800000045', source: 'jdbc', group: 'basis:dept:130101:student_worker'],
          [id: '800000046', source: 'jdbc', group: 'basis:dept:120200:staff'],
          [id: '800000047', source: 'jdbc', group: 'basis:dept:120103:staff'],
          [id: '800000048', source: 'jdbc', group: 'basis:dept:120201:staff'],
          [id: '800000049', source: 'jdbc', group: 'basis:dept:110202:faculty'],
          [id: '800000050', source: 'jdbc', group: 'basis:dept:110102:staff'],
          [id: '800000051', source: 'jdbc', group: 'basis:dept:130100:staff'],
          [id: '800000052', source: 'jdbc', group: 'basis:dept:130000:contractor'],
          [id: '800000053', source: 'jdbc', group: 'basis:dept:110102:contractor'],
          [id: '800000053', source: 'jdbc', group: 'basis:dept:110200:staff'],
          [id: '800000054', source: 'jdbc', group: 'basis:dept:120202:staff'],
          [id: '800000054', source: 'jdbc', group: 'basis:dept:130101:staff'],
          [id: '800000055', source: 'jdbc', group: 'basis:dept:130100:faculty'],
          [id: '800000056', source: 'jdbc', group: 'basis:dept:110201:faculty'],
          [id: '800000057', source: 'jdbc', group: 'basis:dept:110000:student_worker'],
          [id: '800000057', source: 'jdbc', group: 'basis:dept:120202:faculty'],
          [id: '800000058', source: 'jdbc', group: 'basis:dept:100000:affiliate'],
          [id: '800000058', source: 'jdbc', group: 'basis:dept:110103:staff'],
          [id: '800000059', source: 'jdbc', group: 'basis:dept:110101:staff'],
          [id: '800000060', source: 'jdbc', group: 'basis:dept:110200:faculty'],
          [id: '800000060', source: 'jdbc', group: 'basis:dept:120201:affiliate'],
          [id: '800000060', source: 'jdbc', group: 'basis:dept:120203:staff'],
          [id: '800000061', source: 'jdbc', group: 'basis:dept:110100:staff'],
          [id: '800000061', source: 'jdbc', group: 'basis:dept:130202:staff'],
          [id: '800000062', source: 'jdbc', group: 'basis:dept:120000:student_worker'],
          [id: '800000062', source: 'jdbc', group: 'basis:dept:130101:staff'],
          [id: '800000063', source: 'jdbc', group: 'basis:dept:110101:contractor'],
          [id: '800000063', source: 'jdbc', group: 'basis:dept:110203:staff'],
          [id: '800000064', source: 'jdbc', group: 'basis:dept:120103:faculty'],
          [id: '800000065', source: 'jdbc', group: 'basis:dept:110000:staff'],
          [id: '800000065', source: 'jdbc', group: 'basis:dept:110103:staff'],
          [id: '800000066', source: 'jdbc', group: 'basis:dept:110103:staff'],
          [id: '800000066', source: 'jdbc', group: 'basis:dept:130201:affiliate'],
          [id: '800000067', source: 'jdbc', group: 'basis:dept:120101:staff'],
          [id: '800000068', source: 'jdbc', group: 'basis:dept:120100:faculty'],
          [id: '800000069', source: 'jdbc', group: 'basis:dept:130200:contractor'],
          [id: '800000070', source: 'jdbc', group: 'basis:dept:110200:affiliate'],
          [id: '800000070', source: 'jdbc', group: 'basis:dept:120203:staff'],
          [id: '800000071', source: 'jdbc', group: 'basis:dept:110101:staff'],
          [id: '800000071', source: 'jdbc', group: 'basis:dept:130201:affiliate'],
          [id: '800000072', source: 'jdbc', group: 'basis:dept:110102:staff'],
          [id: '800000073', source: 'jdbc', group: 'basis:dept:120000:staff'],
          [id: '800000074', source: 'jdbc', group: 'basis:dept:110201:staff'],
          [id: '800000075', source: 'jdbc', group: 'basis:dept:110202:staff'],
          [id: '800000076', source: 'jdbc', group: 'basis:dept:110202:staff'],
          [id: '800000076', source: 'jdbc', group: 'basis:dept:120203:contractor'],
          [id: '800000077', source: 'jdbc', group: 'basis:dept:110000:staff'],
          [id: '800000077', source: 'jdbc', group: 'basis:dept:110100:staff'],
          [id: '800000078', source: 'jdbc', group: 'basis:dept:110100:staff'],
          [id: '800000079', source: 'jdbc', group: 'basis:dept:120000:staff'],
          [id: '800000080', source: 'jdbc', group: 'basis:dept:130101:affiliate'],
          [id: '800000080', source: 'jdbc', group: 'basis:dept:130102:staff'],
          [id: '800000081', source: 'jdbc', group: 'basis:dept:110000:staff'],
          [id: '800000082', source: 'jdbc', group: 'basis:dept:130201:staff'],
          [id: '800000083', source: 'jdbc', group: 'basis:dept:130000:staff'],
          [id: '800000084', source: 'jdbc', group: 'basis:dept:120102:staff'],
          [id: '800000085', source: 'jdbc', group: 'basis:dept:120000:staff'],
          [id: '800000085', source: 'jdbc', group: 'basis:dept:130203:staff'],
          [id: '800000086', source: 'jdbc', group: 'basis:dept:100000:staff'],
          [id: '800000086', source: 'jdbc', group: 'basis:dept:120101:staff'],
          [id: '800000087', source: 'jdbc', group: 'basis:dept:130103:contractor'],
          [id: '800000087', source: 'jdbc', group: 'basis:dept:130103:faculty'],
          [id: '800000088', source: 'jdbc', group: 'basis:dept:120203:staff'],
          [id: '800000088', source: 'jdbc', group: 'basis:dept:130202:faculty'],
          [id: '800000089', source: 'jdbc', group: 'basis:dept:120100:contractor'],
          [id: '800000089', source: 'jdbc', group: 'basis:dept:130100:staff'],
          [id: '800000090', source: 'jdbc', group: 'basis:dept:130203:contractor'],
          [id: '800000091', source: 'jdbc', group: 'basis:dept:110202:affiliate'],
          [id: '800000091', source: 'jdbc', group: 'basis:dept:130000:staff'],
          [id: '800000092', source: 'jdbc', group: 'basis:dept:110103:faculty'],
          [id: '800000093', source: 'jdbc', group: 'basis:dept:120100:staff'],
          [id: '800000094', source: 'jdbc', group: 'basis:dept:120202:staff'],
          [id: '800000095', source: 'jdbc', group: 'basis:dept:120201:faculty'],
          [id: '800000096', source: 'jdbc', group: 'basis:dept:130000:staff'],
          [id: '800000097', source: 'jdbc', group: 'basis:dept:130202:staff'],
          [id: '800000098', source: 'jdbc', group: 'basis:dept:110200:staff'],
          [id: '800000099', source: 'jdbc', group: 'basis:dept:120202:staff'],
          [identifier: 'basis:org:120100:contractor', source: 'g:gsa', group: 'app:its:cttoday:etc:updaters'],
          [identifier: 'basis:org:120100:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'basis:dept:110000:staff', source: 'g:gsa', group: 'basis:org:110000:employee'],
          [identifier: 'basis:dept:110000:staff', source: 'g:gsa', group: 'basis:org:110000:staff'],
          [identifier: 'basis:dept:110000:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:130000:contractor', source: 'g:gsa', group: 'basis:org:130000:affiliate'],
          [identifier: 'basis:dept:130000:contractor', source: 'g:gsa', group: 'basis:org:130000:contractor'],
          [identifier: 'basis:dept:130000:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:dept:120102:staff', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120102:staff', source: 'g:gsa', group: 'basis:org:120100:staff'],
          [identifier: 'basis:dept:120102:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:130200:affiliate', source: 'g:gsa', group: 'app:its:cttoday:etc:readers'],
          [identifier: 'basis:org:130200:affiliate', source: 'g:gsa', group: 'app:its:nistaff:etc:updaters'],
          [identifier: 'basis:org:130200:affiliate', source: 'g:gsa', group: 'app:its:nsreturn:etc:readers'],
          [identifier: 'basis:org:130200:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:org:130100:employee', source: 'g:gsa', group: 'app:its:cttoday:etc:readers'],
          [identifier: 'basis:org:130100:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:dept:110000:student_worker', source: 'g:gsa', group: 'basis:org:110000:student_worker'],
          [identifier: 'basis:dept:110000:student_worker', source: 'g:gsa', group: 'ref:job:by-role:all-student_worker'],
          [identifier: 'basis:dept:120203:staff', source: 'g:gsa', group: 'basis:org:120200:employee'],
          [identifier: 'basis:dept:120203:staff', source: 'g:gsa', group: 'basis:org:120200:staff'],
          [identifier: 'basis:dept:120203:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:120202:staff', source: 'g:gsa', group: 'basis:org:120200:employee'],
          [identifier: 'basis:dept:120202:staff', source: 'g:gsa', group: 'basis:org:120200:staff'],
          [identifier: 'basis:dept:120202:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:110101:staff', source: 'g:gsa', group: 'basis:org:110100:employee'],
          [identifier: 'basis:dept:110101:staff', source: 'g:gsa', group: 'basis:org:110100:staff'],
          [identifier: 'basis:dept:110101:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:130200:faculty', source: 'g:gsa', group: 'basis:org:130200:employee'],
          [identifier: 'basis:dept:130200:faculty', source: 'g:gsa', group: 'basis:org:130200:faculty'],
          [identifier: 'basis:dept:130200:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:100000:staff', source: 'g:gsa', group: 'basis:org:100000:employee'],
          [identifier: 'basis:dept:100000:staff', source: 'g:gsa', group: 'basis:org:100000:staff'],
          [identifier: 'basis:dept:100000:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', group: 'basis:org:110100:employee'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', group: 'basis:org:110100:staff'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'readers', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'readers', group: 'app:its:nsreturn:Next Student Return Authorized'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'readers', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'readers', group: 'basis:dept:110100:staff'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'readers', group: 'basis:org:110100:affiliate'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'readers', group: 'basis:org:110100:employee'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'readers', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'viewers', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'viewers', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'viewers', group: 'basis:dept:110100:affiliate'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'viewers', group: 'basis:org:110100:staff'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'viewers', group: 'etc:loader:basis:depts'],
          [identifier: 'basis:dept:110100:staff', source: 'g:gsa', privileges: 'viewers', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:100000:contractor', source: 'g:gsa', group: 'basis:org:100000:contractor'],
          [identifier: 'basis:dept:100000:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:dept:110203:staff', source: 'g:gsa', group: 'basis:org:110200:employee'],
          [identifier: 'basis:dept:110203:staff', source: 'g:gsa', group: 'basis:org:110200:staff'],
          [identifier: 'basis:dept:110203:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:130203:contractor', source: 'g:gsa', group: 'basis:org:130200:contractor'],
          [identifier: 'basis:dept:130203:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:org:110100:employee', source: 'g:gsa', group: 'app:its:lbsimply:etc:updaters'],
          [identifier: 'basis:org:110100:employee', source: 'g:gsa', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:org:110100:employee', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:110100:employee', source: 'g:gsa', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:110100:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:org:110000:affiliate', source: 'g:gsa', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:org:110000:affiliate', source: 'g:gsa', group: 'app:its:nistaff:etc:readers'],
          [identifier: 'basis:org:110000:affiliate', source: 'g:gsa', group: 'app:its:nistaff:etc:updaters'],
          [identifier: 'basis:org:110000:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:org:120000:affiliate', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:120000:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:dept:120101:staff', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120101:staff', source: 'g:gsa', group: 'basis:org:120100:staff'],
          [identifier: 'basis:dept:120101:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:120201:student_worker', source: 'g:gsa', group: 'basis:org:120200:student_worker'],
          [identifier: 'basis:dept:120201:student_worker', source: 'g:gsa', group: 'ref:job:by-role:all-student_worker'],
          [identifier: 'basis:dept:130103:faculty', source: 'g:gsa', group: 'basis:org:130100:employee'],
          [identifier: 'basis:dept:130103:faculty', source: 'g:gsa', group: 'basis:org:130100:faculty'],
          [identifier: 'basis:dept:130103:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:120103:student_worker', source: 'g:gsa', group: 'basis:org:120100:affiliate'],
          [identifier: 'basis:dept:120103:student_worker', source: 'g:gsa', group: 'basis:org:120100:student_worker'],
          [identifier: 'basis:dept:120103:student_worker', source: 'g:gsa', group: 'ref:job:by-role:all-student_worker'],
          [identifier: 'basis:dept:120200:faculty', source: 'g:gsa', group: 'basis:org:120200:employee'],
          [identifier: 'basis:dept:120200:faculty', source: 'g:gsa', group: 'basis:org:120200:faculty'],
          [identifier: 'basis:dept:120200:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:130100:contractor', source: 'g:gsa', group: 'basis:org:130100:contractor'],
          [identifier: 'basis:dept:130100:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:dept:110101:contractor', source: 'g:gsa', group: 'basis:org:110100:contractor'],
          [identifier: 'basis:dept:110101:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:dept:130200:affiliate', source: 'g:gsa', group: 'basis:org:130200:affiliate'],
          [identifier: 'basis:dept:130200:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:org:100000:staff', source: 'g:gsa', group: 'app:its:pcavoid:etc:updaters'],
          [identifier: 'basis:org:100000:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'app:its:lbsimply:etc:readers', source: 'g:gsa', privileges: 'readers', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:dept:130202:faculty', source: 'g:gsa', group: 'basis:org:130200:employee'],
          [identifier: 'basis:dept:130202:faculty', source: 'g:gsa', group: 'basis:org:130200:faculty'],
          [identifier: 'basis:dept:130202:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:org:130000:staff', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:130000:staff', source: 'g:gsa', group: 'app:its:pcavoid:etc:readers'],
          [identifier: 'basis:org:130000:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:org:110100:faculty', source: 'g:gsa', group: 'app:its:lbsimply:etc:updaters'],
          [identifier: 'basis:org:110100:faculty', source: 'g:gsa', group: 'app:its:nistaff:sources'],
          [identifier: 'basis:org:110100:faculty', source: 'g:gsa', group: 'app:its:pcavoid:etc:updaters'],
          [identifier: 'basis:org:110100:faculty', source: 'g:gsa', group: 'ref:job:by-category:all-faculty'],
          [identifier: 'basis:org:130100:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:dept:120100:staff', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120100:staff', source: 'g:gsa', group: 'basis:org:120100:staff'],
          [identifier: 'basis:dept:120100:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:120103:faculty', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120103:faculty', source: 'g:gsa', group: 'basis:org:120100:faculty'],
          [identifier: 'basis:dept:120103:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:130103:contractor', source: 'g:gsa', group: 'basis:org:130100:contractor'],
          [identifier: 'basis:dept:130103:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:org:130100:faculty', source: 'g:gsa', group: 'app:its:nsreturn:etc:updaters'],
          [identifier: 'basis:org:130100:faculty', source: 'g:gsa', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:130100:faculty', source: 'g:gsa', group: 'ref:job:by-category:all-faculty'],
          [identifier: 'basis:org:120100:staff', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:120100:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:org:130200:staff', source: 'g:gsa', group: 'app:its:pcavoid:etc:readers'],
          [identifier: 'basis:org:130200:staff', source: 'g:gsa', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:130200:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:dept:110200:affiliate', source: 'g:gsa', group: 'basis:org:110200:affiliate'],
          [identifier: 'basis:dept:110200:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:120201:faculty', source: 'g:gsa', group: 'basis:org:120200:employee'],
          [identifier: 'basis:dept:120201:faculty', source: 'g:gsa', group: 'basis:org:120200:faculty'],
          [identifier: 'basis:dept:120201:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:org:120100:employee', source: 'g:gsa', group: 'app:its:lbsimply:etc:readers'],
          [identifier: 'basis:org:120100:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:dept:110202:affiliate', source: 'g:gsa', group: 'basis:org:110200:affiliate'],
          [identifier: 'basis:dept:110202:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:120200:contractor', source: 'g:gsa', group: 'basis:org:120200:contractor'],
          [identifier: 'basis:dept:120200:contractor', source: 'g:gsa', group: 'ref:job:by-role:all-contractor'],
          [identifier: 'basis:org:130000:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:dept:120201:affiliate', source: 'g:gsa', group: 'basis:org:120200:affiliate'],
          [identifier: 'basis:dept:120201:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:org:120200:affiliate', source: 'g:gsa', group: 'app:its:nsreturn:etc:updaters'],
          [identifier: 'basis:org:120200:affiliate', source: 'g:gsa', group: 'app:its:pcavoid:etc:readers'],
          [identifier: 'basis:org:120200:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:org:130100:contractor', source: 'g:gsa', group: 'app:its:nistaff:etc:readers'],
          [identifier: 'basis:org:130100:contractor', source: 'g:gsa', group: 'ref:job:by-category:all-contractor'],
          [identifier: 'basis:dept:120200:staff', source: 'g:gsa', group: 'basis:org:120200:employee'],
          [identifier: 'basis:dept:120200:staff', source: 'g:gsa', group: 'basis:org:120200:staff'],
          [identifier: 'basis:dept:120200:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:120100:affiliate', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:120100:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'basis:dept:130000:staff', source: 'g:gsa', group: 'basis:org:130000:employee'],
          [identifier: 'basis:dept:130000:staff', source: 'g:gsa', group: 'basis:org:130000:staff'],
          [identifier: 'basis:dept:130000:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:110103:faculty', source: 'g:gsa', group: 'basis:org:110100:employee'],
          [identifier: 'basis:dept:110103:faculty', source: 'g:gsa', group: 'basis:org:110100:faculty'],
          [identifier: 'basis:dept:110103:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:org:120000:employee', source: 'g:gsa', group: 'app:its:cttoday:etc:readers'],
          [identifier: 'basis:org:120000:employee', source: 'g:gsa', group: 'app:its:nsreturn:etc:readers'],
          [identifier: 'basis:org:120000:employee', source: 'g:gsa', group: 'ref:job:by-category:all-employee'],
          [identifier: 'basis:org:100000:affiliate', source: 'g:gsa', group: 'app:its:nsreturn:etc:readers'],
          [identifier: 'basis:org:100000:affiliate', source: 'g:gsa', group: 'app:its:pcavoid:sources'],
          [identifier: 'basis:org:100000:affiliate', source: 'g:gsa', group: 'ref:job:by-category:all-affiliate'],
          [identifier: 'app:its:lbsimply:etc:updaters', source: 'g:gsa', privileges: 'updaters', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:dept:130200:staff', source: 'g:gsa', group: 'basis:org:130200:employee'],
          [identifier: 'basis:dept:130200:staff', source: 'g:gsa', group: 'basis:org:130200:staff'],
          [identifier: 'basis:dept:130200:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:130201:affiliate', source: 'g:gsa', group: 'basis:org:130200:affiliate'],
          [identifier: 'basis:dept:130201:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:130203:affiliate', source: 'g:gsa', group: 'basis:org:130200:affiliate'],
          [identifier: 'basis:dept:130203:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'basis:dept:130202:staff', source: 'g:gsa', group: 'basis:org:130200:employee'],
          [identifier: 'basis:dept:130202:staff', source: 'g:gsa', group: 'basis:org:130200:staff'],
          [identifier: 'basis:dept:130202:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:org:120000:staff', source: 'g:gsa', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:org:120000:staff', source: 'g:gsa', group: 'app:its:nistaff:etc:updaters'],
          [identifier: 'basis:org:120000:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:dept:110102:staff', source: 'g:gsa', group: 'basis:org:110100:employee'],
          [identifier: 'basis:dept:110102:staff', source: 'g:gsa', group: 'basis:org:110100:staff'],
          [identifier: 'basis:dept:110102:staff', source: 'g:gsa', group: 'ref:job:by-role:all-staff'],
          [identifier: 'basis:dept:120202:faculty', source: 'g:gsa', group: 'basis:org:120200:employee'],
          [identifier: 'basis:dept:120202:faculty', source: 'g:gsa', group: 'basis:org:120200:faculty'],
          [identifier: 'basis:dept:120202:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:dept:120102:faculty', source: 'g:gsa', group: 'basis:org:120100:employee'],
          [identifier: 'basis:dept:120102:faculty', source: 'g:gsa', group: 'basis:org:120100:faculty'],
          [identifier: 'basis:dept:120102:faculty', source: 'g:gsa', group: 'ref:job:by-role:all-faculty'],
          [identifier: 'basis:org:110200:staff', source: 'g:gsa', group: 'app:its:nistaff:etc:readers'],
          [identifier: 'basis:org:110200:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'basis:org:120200:staff', source: 'g:gsa', group: 'app:its:lbsimply:sources'],
          [identifier: 'basis:org:120200:staff', source: 'g:gsa', group: 'app:its:nsreturn:sources'],
          [identifier: 'basis:org:120200:staff', source: 'g:gsa', group: 'ref:job:by-category:all-staff'],
          [identifier: 'app:its:cttoday:etc:updaters', source: 'g:gsa', privileges: 'updaters', group: 'app:its:cttoday:sources'],
          [identifier: 'basis:dept:110100:affiliate', source: 'g:gsa', group: 'basis:org:110100:affiliate'],
          [identifier: 'basis:dept:110100:affiliate', source: 'g:gsa', group: 'ref:job:by-role:all-affiliate'],
          [identifier: 'app:its:nistaff:etc:readers', source: 'g:gsa', privileges: 'readers', group: 'app:its:nistaff:sources'],
  ]

  memberData.each { row ->
    Subject subject = null
    if (row.id != null) {
      subject = SubjectFinder.findByIdAndSource(row.id, row.source, false)
    } else if (row.identifier != null) {
      subject = SubjectFinder.findByIdentifierAndSource(row.identifier, row.source, false)
    }
    if (subject == null) {
      stats.gshTotalErrorCount++
      println "Error: cant find group subject: ${row.source}: id: ${row.id}, identifier: ${row.identifier}"
    }
    Group group = GroupFinder.findByName(grouperSession, row.group, false)
    Privilege privilege = null
    if (row.privileges != null) {
      privilege = Privilege.listToPriv(row.privileges, false)
    }

    if (subject != null) {
      if (group != null) {
        if (privilege != null) {
          boolean changed = group.grantPriv(subject, privilege, false)
          stats.gshTotalObjectCount++
          if (changed) {
            stats.gshTotalChangeCount++
            println "Made change for group privilege: ${group.name}, privilege: ${privilege}, subject: ${GrouperUtil.subjectToString(subject)}"
          }
        } else {
          boolean changed = group.addOrEditMember(subject, false, true, null, null, false)
          stats.gshTotalObjectCount++
          if (changed) {
            stats.gshTotalChangeCount++
            println "Made change for group membership: ${group.name}, field: members, subject: ${GrouperUtil.subjectToString(subject)}"
          }
        }
      } else {
        stats.gshTotalErrorCount++
        println "ERROR: cant find group: '${row.group}'"
      }
    } else {
      stats.gshTotalErrorCount++
      println "ERROR: cant find subject: '${row.subject}' in source ${row.source}"
    }
  }
  println "${new Date()} Done with memberships and privileges, objects: ${stats.gshTotalObjectCount}, expected approx total: 706, changes: ${stats.gshTotalChangeCount}, known errors (view output for full list): ${stats.gshTotalErrorCount}"
}

def loadSampleAttributeAssignments(grouperSession, stats) {
  //Set attributeAssignIdsAlreadyUsed = []

  //create a map of loader group name to group id. These will be used to set loaded group grouperLoaderMetadataGroupId
  // to the correct uuid of the loader job, since it will be different for every run
  // yields map e.g. [etc:loader:basis:depts: '8acdedf15cf14797b826d1687042bae0', ...]
  def loaderRef = ['etc:loader:basis:depts', 'etc:loader:basis:orgs', 'etc:loader:ref:job-categories', 'etc:loader:ref:job-roles'].collectEntries {
    [(it): GroupFinder.findByName(grouperSession, it, false).id]
  }

  def attrAssignData = [
          [name: 'etc:attribute:loaderMetadata:loaderMetadata', group: 'basis:org:120000:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977884'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata', group: 'basis:org:120000:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977884'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:legacy:attribute:legacyGroupType_grouperLoader',  group: 'etc:loader:basis:depts', attrAssigns:[
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderType', value: 'SQL_GROUP_LIST'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderGroupQuery', value: """\nselect distinct\n  'basis:dept:' || d.id || ':' || replace(r.name, ' ', '_') as group_name,\n  'Basis:Departments:'\n      || d.id || ' - ' || replace(d.name, ':', '_') || ':'\n      || initcap(r.name) || ' - ' || replace(d.name, ':', '_') as group_display_name\n from mock_person_jobs pj\n join mock_jobs j on pj.job_id = j.id\n join mock_depts d on j.dept_id = d.id\n join mock_job_roles r on j.job_role_id = r.id\n"""],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron', value: '59 59 23 31 12 ? 2099'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderScheduleType', value: 'CRON'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuery', value: """\nselect distinct\n  'basis:dept:' || d.id || ':' || replace(r.name, ' ', '_') as group_name,\n  pj.person_id as subject_id\nfrom mock_person_jobs pj\njoin mock_jobs j on pj.job_id = j.id\njoin mock_depts d on j.dept_id = d.id\njoin mock_job_roles r on j.job_role_id = r.id\n"""],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderDbName', value: 'grouper'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130000:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235371152'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130100:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965679'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110100:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977820'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120200:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964751'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120200:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978629'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130200:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978875'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130203:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966283'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120200:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964910'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130102:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965893'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-category:all-faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 6 inserted: 6 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194991490'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-categories'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130100:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965283'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120200:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 7 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235371286'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110200:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963706'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130000:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965320'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110100:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977948'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-role:all-student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'true'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194997447'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-roles'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 6 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120203:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 5 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965445'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130202:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966129'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130100:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965562'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964526'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:100000:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963194'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120201:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965108'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130000:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978589'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:100000:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235370581'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-role:all-affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194997424'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 11 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-roles'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'true'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120201:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965103'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110102:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963074'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110203:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964318'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130200:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966050'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130100:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978609'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130103:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966150'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:legacy:attribute:legacyGroupType_grouperLoader',  group: 'etc:loader:basis:orgs', attrAssigns:[
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderScheduleType', value: 'CRON'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderType', value: 'SQL_GROUP_LIST'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderDbName', value: 'grouper'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron', value: '59 59 23 31 12 ? 2099'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderGroupQuery', value: """\nselect distinct\n  'basis:org:' || d_org.id || ':' || replace(r.name, ' ', '_') as group_name,\n  'Basis:Organizations:Org ' || d_org.id || ' - ' || replace(d_org.name, ':', '_') || ':' || initcap(r.name) as group_display_name\n from mock_person_jobs pj\n join mock_jobs j on pj.job_id = j.id\n join mock_job_roles r on j.job_role_id = r.id\n join mock_depts d on j.dept_id = d.id\n join mock_depts d_org on d.umbrella_dept_id = d_org.id\n"""],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuery', value: """\nselect distinct\n  'basis:org:' || d_org.id || ':' || replace(r.name, ' ', '_') as group_name,\n  'basis:dept:' || d.id || ':' || replace(r.name, ' ', '_') as subject_identifier,\n  'g:gsa' as subject_source_id\n from mock_person_jobs pj\n join mock_jobs j on pj.job_id = j.id\n join mock_job_roles r on j.job_role_id = r.id\n join mock_depts d on j.dept_id = d.id\n join mock_depts d_org on d.umbrella_dept_id = d_org.id\n"""],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130100:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978741'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120102:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964483'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120102:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964473'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120103:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964725'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130000:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235370833'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:legacy:attribute:legacyGroupType_grouperLoader',  group: 'etc:loader:ref:job-categories', attrAssigns:[
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderScheduleType', value: 'CRON'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderGroupQuery', value: """\nselect distinct\n 'ref:job:by-category:all-' || extension as group_name,\n 'Reference:Jobs:Jobs By Category:All ' || initcap(extension) as group_display_name\n from grouper_groups where name like 'basis:org:%:%'\n """],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderDbName', value: 'grouper'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuery', value: """\nselect distinct\n 'ref:job:by-category:all-' || extension as group_name,\n name as subject_identifier,\n  'g:gsa' as subject_source_id\n from grouper_groups where name like 'basis:org:%:%'\n """],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderType', value: 'SQL_GROUP_LIST'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron', value: '59 59 23 31 12 ? 2099'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120000:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235370958'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110200:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963859'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110200:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 7 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235371173'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977971'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120100:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964257'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:100000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963193'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120202:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965652'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-category:all-staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194991497'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-categories'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 10 inserted: 10 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130200:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965954'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130200:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978979'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130200:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966036'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130200:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978755'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120103:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964603'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-role:all-staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 28 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'true'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-roles'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194997443'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110100:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963510'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-category:all-student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 6 inserted: 6 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194991494'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-categories'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130202:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966198'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130103:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965892'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978599'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120101:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964987'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:100000:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978198'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:100000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977945'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110200:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963556'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110102:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963634'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130201:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966257'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110202:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963976'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130202:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966215'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130201:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966298'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130100:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978753'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-category:all-affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194988725'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-categories'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 10 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110202:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963477'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130100:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 6 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235370593'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120100:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235371080'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 8 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130101:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965626'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120200:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964950'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120103:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964508'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120000:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235371257'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120202:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965368'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110000:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977968'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:100000:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963246'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978057'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965496'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110103:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963427'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 5 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120200:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978427'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120203:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965638'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110000:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963876'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120101:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964729'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110103:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963277'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110200:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977795'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110203:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964240'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110100:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 5 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235370987'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120100:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977895'],
          ]],
          [name: 'etc:legacy:attribute:legacyGroupType_grouperLoader',  group: 'etc:loader:ref:job-roles', attrAssigns:[
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderScheduleType', value: 'CRON'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderDbName', value: 'grouper'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuery', value: """\nselect distinct\n 'ref:job:by-role:all-' || extension as group_name,\n name as subject_identifier,\n  'g:gsa' as subject_source_id\n from grouper_groups where name like 'basis:dept:%:%'\n """],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderGroupQuery', value: """\nselect distinct\n 'ref:job:by-role:all-' || extension as group_name,\n 'Reference:Jobs:Jobs By Role:All ' || initcap(extension) as group_display_name\n from grouper_groups where name like 'basis:dept:%:%'\n """],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderQuartzCron', value: '59 59 23 31 12 ? 2099'],
                  [name: 'etc:legacy:attribute:legacyAttribute_grouperLoaderType', value: 'SQL_GROUP_LIST'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130200:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966122'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:pspng:provision_to',  group: 'basis:org:110100:affiliate', attrAssignValues:['testDB2']],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110200:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977818'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130200:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978895'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130200:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 6 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235370577'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130101:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966018'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110000:employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235371062'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110100:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977926'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120202:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965705'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120200:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977961'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110201:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963783'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120100:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964851'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120201:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965152'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-category:all-contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-categories'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 8 inserted: 8 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194991494'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:100000:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977898'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-category:all-employee', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-categories'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194988683'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 10 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130103:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965870'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110100:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977831'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110201:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964270'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120200:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978584'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120100:faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977802'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110100:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963859'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110000:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963409'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130203:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966239'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110200:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977847'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130100:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978734'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130200:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978914'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-role:all-contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 13 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'true'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194997443'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-roles'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110200:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977851'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120200:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978548'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110000:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963660'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:130100:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978743'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120000:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964191'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:110000:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978059'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'ref:job:by-role:all-faculty', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 15 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:ref:job-roles'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'true'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194997483'],
          ]],
          [name: 'etc:pspng:provision_to',  group: 'basis:org:110100:faculty', attrAssignValues:['testDB2']],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130203:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966295'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130101:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 3 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965679'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120201:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194965045'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110101:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963193'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120100:student_worker', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194977772'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:130201:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194966338'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110101:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194963337'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120100:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194978030'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:120100:contractor', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 1 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964950'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:dept:110202:staff', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 4 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:depts'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1545194964246'],
          ]],
          [name: 'etc:attribute:loaderMetadata:loaderMetadata',  group: 'basis:org:120100:affiliate', attrAssigns:[
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastFullMillisSince1970', value: '1544235371267'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLoaded', value: 'false'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataLastSummary', value: 'total: 2 inserted: 0 deleted: 0 updated: 0'],
                  [name: 'etc:attribute:loaderMetadata:grouperLoaderMetadataGroupId', loaderRef: 'etc:loader:basis:orgs'],
          ]],
  ]

  attrAssignData.each { row ->
    boolean problemWithAttributeAssign = false
    AttributeAssignSave attributeAssignSave = new AttributeAssignSave(grouperSession).assignPrintChangesToSystemOut(true)
    //attributeAssignSave.assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed)
    attributeAssignSave.assignAttributeAssignType(AttributeAssignType.group)
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(row.name, false)
    if (attributeDefName == null) {
      stats.gshTotalErrorCount++
      println "Error: cant find attributeDefName: ${row.name}"
      problemWithAttributeAssign = true
    }
    attributeAssignSave.assignAttributeDefName(attributeDefName)
    Group ownerGroup = GroupFinder.findByName(grouperSession, row.group, false)
    if (ownerGroup == null) {
      stats.gshTotalErrorCount++
      println "Error: cant find group: ${row.group}"
      problemWithAttributeAssign = true
    }
    attributeAssignSave.assignOwnerGroup(ownerGroup)
    //attributeAssignSave.assignPutAttributeAssignIdsToNotUseSet(true)
    stats.gshTotalObjectCount++
    if (row.attrAssignValues != null) {
      row.attrAssignValues.each { value ->
        attributeAssignSave.addValue(value)
        stats.gshTotalObjectCount++
      }
    }
    else if (row.attrAssigns != null) {
      row.attrAssigns.each { aa ->
        stats.gshTotalObjectCount++
        attributeAssignOnAssignSave = new AttributeAssignSave(grouperSession).assignPrintChangesToSystemOut(true)
        //attributeAssignOnAssignSave.assignAttributeAssignIdsToNotUse(attributeAssignIdsAlreadyUsed)
        attributeAssignOnAssignSave.assignAttributeAssignType(AttributeAssignType.group_asgn)
        attributeDefName = AttributeDefNameFinder.findByName(aa.name, false)
        if (attributeDefName == null) {
          stats.gshTotalErrorCount++
          println "Error: cant find attributeDefName: ${aa.name}"
          problemWithAttributeAssign = true
        }
        attributeAssignOnAssignSave.assignAttributeDefName(attributeDefName);
        //attributeAssignOnAssignSave.assignPutAttributeAssignIdsToNotUseSet(true);
        if (aa.loaderRef != null) {
          attributeAssignOnAssignSave.addValue(loaderRef[aa.loaderRef])
        } else {
          attributeAssignOnAssignSave.addValue(aa.value)
        }
        attributeAssignSave.addAttributeAssignOnThisAssignment(attributeAssignOnAssignSave)
        stats.gshTotalObjectCount++
      }
    }
    if (!problemWithAttributeAssign) {
      AttributeAssign attributeAssign = attributeAssignSave.save()
      if (attributeAssignSave.getChangesCount() > 0) {
        stats.gshTotalChangeCount += attributeAssignSave.getChangesCount()
        println "Made ${attributeAssignSave.getChangesCount()} changes for attribute assign: ${attributeAssign.toString()}"
      }
    }
    problemWithAttributeAssign = false
  }

  println "${new Date()} Script complete: total objects, objects: ${stats.gshTotalObjectCount}, expected approx total: 1977, changes: ${stats.gshTotalChangeCount}, known errors (view output for full list): ${stats.gshTotalErrorCount}"
}


/* == Main == */

GrouperSession grouperSession = GrouperSession.startRootSession()

loadSampleSubjects(grouperSession)
loadSampleFolders(grouperSession, stats)
loadSampleGroups(grouperSession, stats)
loadSampleComposites(grouperSession, stats)
loadSampleAttributeDefs(grouperSession, stats)
loadSampleMembershipsAndPrivileges(grouperSession, stats)
loadSampleAttributeAssignments(grouperSession, stats)

