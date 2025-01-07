/*
 * SonarQube Java
 * Copyright (C) 2012-2025 SonarSource SA
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
package org.sonar.java.se.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.se.SECheckVerifier;
import org.sonar.java.se.utils.SETestUtils;
import org.sonar.java.checks.verifier.TestUtils;

class StreamConsumedCheckTest {

  @Test
  void test() {
    SECheckVerifier.newVerifier()
      .onFile(TestUtils.mainCodeSourcesPath("symbolicexecution/checks/S3959_StreamConsumedCheckSample.java"))
      .withCheck(new StreamConsumedCheck())
      .withClassPath(SETestUtils.CLASS_PATH)
      .verifyIssues();
  }

  @Test
  void swtichExpression() {
    SECheckVerifier.newVerifier()
      .onFile(TestUtils.nonCompilingTestSourcesPath("symbolicexecution/checks/S3959_SwitchExpressionSample.java"))
      .withCheck(new StreamConsumedCheck())
      .withClassPath(SETestUtils.CLASS_PATH)
      .withJavaVersion(14)
      .verifyIssues();
  }
}
