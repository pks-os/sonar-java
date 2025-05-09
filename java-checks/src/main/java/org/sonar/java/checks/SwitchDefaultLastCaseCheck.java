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
package org.sonar.java.checks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.java.ast.api.JavaKeyword;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.CaseGroupTree;
import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.SwitchTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "S4524")
public class SwitchDefaultLastCaseCheck extends IssuableSubscriptionVisitor {

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Arrays.asList(Tree.Kind.SWITCH_STATEMENT, Tree.Kind.SWITCH_EXPRESSION);
  }

  @Override
  public void visitNode(Tree tree) {
    SwitchTree switchTree = (SwitchTree) tree;
    getDefaultLabelAtWrongPosition(switchTree).ifPresent(defaultLabel -> reportIssue(defaultLabel, "Move this default to the end of the switch."));
  }

  private static Optional<CaseLabelTree> getDefaultLabelAtWrongPosition(SwitchTree switchTree) {
    List<CaseGroupTree> cases = switchTree.cases();
    for (int i = 0; i < cases.size(); i++) {
      List<CaseLabelTree> labels = cases.get(i).labels();
      for (int j = 0; j < labels.size(); j++) {
        CaseLabelTree label = labels.get(j);
        boolean defaultExists = isDefault(label);
        if (defaultExists && ((j != labels.size() - 1) || (i == cases.size() - 1))) {
          /*
           * we return Optional.empty() because either we have default at the end which is a best practise
           * or it is in a place in a case group where it can not affect the result of the execution
           */
          return Optional.empty();
        } else if (defaultExists) {
          return Optional.of(label);
        }
      }
    }
    return Optional.empty();
  }

  private static boolean isDefault(CaseLabelTree caseLabelTree) {
    return JavaKeyword.DEFAULT.getValue().equals(caseLabelTree.caseOrDefaultKeyword().text());
  }
}
