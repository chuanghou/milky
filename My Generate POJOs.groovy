import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 * From database to Java, using this groovy, detail manual book, https://zhuanlan.zhihu.com/p/615921350?utm_id=0
 * From Java bean to DDL JavaBean2DDL plugin, detail manual book, https://plugins.jetbrains.com/plugin/15980-javabean2ddl
 */

packageName = "com.sample;"
typeMapping = [
   (~/(?i)tinyint/)                 : "Boolean",
  (~/(?i)int|smallint unsigned/)    : "Integer",
  (~/(?i)bigint/)                   : "Long",
  (~/(?i)float|double|decimal|real/): "Double",
  (~/(?i)datetime|timestamp/)       : "java.util.Date",
  (~/(?i)/)                         : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
  SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
  def className = javaName(table.getName(), true)
  def fields = calcFields(table)
  new File(dir, className + ".java").withPrintWriter { out -> generate(out, className, fields) }
}

def generate(out, className, fields) {
  out.println "package $packageName"
  out.println ""
  out.println "import java.util.Date;"
  out.println ""
  out.println "public class $className {"
  out.println ""
  fields.each() {
    if (it.annos != "") out.println "  ${it.annos}"
    out.println "  ${it.type} ${it.name};"
  }
  out.println ""
  out.println "}"
}

def calcFields(table) {
  DasUtil.getColumns(table).reduce([]) { fields, col ->
    def spec = Case.LOWER.apply(col.getDasType().getSpecification())
    def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
    fields += [[
                 name : javaName(col.getName(), false),
                 type : typeStr,
                 annos: ""]]
  }
}

def javaName(str, capitalize) {
  def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
    .collect { Case.LOWER.apply(it).capitalize() }
    .join("")
    .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
  capitalize || s.length() == 1? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
