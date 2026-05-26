package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of a Java compile attempt performed by GshTemplateJavaCompiler. On
 * success, classNameToBytecode contains the bytecode for every produced class
 * (the top-level class plus any inner / anonymous / lambda-generated classes,
 * which the compiler emits as separate class files). On failure, the bytecode
 * map is empty and the diagnostics list explains why.
 *
 * The diagnostics list is populated whether or not the compile succeeded —
 * warnings, mandatory warnings, and notes may accompany a successful compile.
 *
 * GRP-7006
 */
public class GshTemplateCompileResult {

  /**
   * true if the compile succeeded — javac returned ok AND at least one class
   * file was produced
   */
  private boolean success;

  /**
   * compiled bytecode keyed by fully-qualified class name; includes the
   * top-level class and any inner / anonymous / lambda classes the compiler
   * produced. Empty if !success.
   */
  private Map<String, byte[]> classNameToBytecode = new LinkedHashMap<>();

  /**
   * diagnostics emitted by the compiler — errors, warnings, mandatory
   * warnings, notes. Never null; may be empty on a clean compile.
   */
  private List<GshTemplateCompileDiagnostic> diagnostics = new ArrayList<>();

  /**
   * true if the compile succeeded
   * @return success
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * @param success
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /**
   * compiled bytecode keyed by fully-qualified class name
   * @return class name to bytecode map
   */
  public Map<String, byte[]> getClassNameToBytecode() {
    return classNameToBytecode;
  }

  /**
   * @param classNameToBytecode
   */
  public void setClassNameToBytecode(Map<String, byte[]> classNameToBytecode) {
    this.classNameToBytecode = classNameToBytecode;
  }

  /**
   * all diagnostics from the compile attempt (errors + warnings + notes)
   * @return diagnostics
   */
  public List<GshTemplateCompileDiagnostic> getDiagnostics() {
    return diagnostics;
  }

  /**
   * @param diagnostics
   */
  public void setDiagnostics(List<GshTemplateCompileDiagnostic> diagnostics) {
    this.diagnostics = diagnostics;
  }

  /**
   * convenience: only the diagnostics with severity ERROR
   * @return error diagnostics
   */
  public List<GshTemplateCompileDiagnostic> errorDiagnostics() {
    List<GshTemplateCompileDiagnostic> errors = new ArrayList<>();
    for (GshTemplateCompileDiagnostic diagnostic : this.diagnostics) {
      if (diagnostic.isError()) {
        errors.add(diagnostic);
      }
    }
    return errors;
  }

  /**
   * convenience: true if at least one diagnostic has severity ERROR
   * @return has errors
   */
  public boolean hasErrors() {
    for (GshTemplateCompileDiagnostic diagnostic : this.diagnostics) {
      if (diagnostic.isError()) {
        return true;
      }
    }
    return false;
  }

}
