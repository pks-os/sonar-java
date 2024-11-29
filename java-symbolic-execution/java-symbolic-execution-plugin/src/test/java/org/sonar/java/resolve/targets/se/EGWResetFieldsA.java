/*
 * SonarQube Java
 * Copyright (C) 2012-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.java.resolve.targets.se;

class EGWResetFieldsA {
  Object field;
  Object a, b, c, d;

  void foo() {
    a = b;
    c = a;
    b = c; // Noncompliant
  }

  boolean isThereANext() {
    doSomething();
    return field != null;
  }

  void doSomething() {
    field = new Object();
  }
}

class EGWResetFieldsC extends EGWResetFieldsA {
  void doSemethingElse() {
    Object o = field;
    while (isThereANext()) {
      doSomething();
      o = field; // Compliant
    }
    field = o; // Compliant
  }
}
