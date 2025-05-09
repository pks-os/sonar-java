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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.java.checks.helpers.QuickFixHelper;
import org.sonar.java.reporting.JavaQuickFix;
import org.sonar.java.reporting.JavaTextEdit;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.JavaVersion;
import org.sonar.plugins.java.api.JavaVersionAwareVisitor;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.CaseGroupTree;
import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.GuardedPatternTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.NullPatternTree;
import org.sonar.plugins.java.api.tree.Tree;


@Rule(key = "S6916")
public class SingleIfInsteadOfPatternMatchGuardCheck extends IssuableSubscriptionVisitor implements JavaVersionAwareVisitor {

  private static final String ISSUE_MESSAGE_REPLACE = "Replace this \"if\" statement with a pattern match guard.";
  private static final String ISSUE_MESSAGE_MERGE = "Merge this \"if\" statement with the enclosing pattern match guard.";

  @Override
  public boolean isCompatibleWithJavaVersion(JavaVersion version) {
    return version.isJava21Compatible();
  }

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return List.of(Tree.Kind.CASE_GROUP);
  }

  @Override
  public void visitNode(Tree tree) {
    var caseGroup = (CaseGroupTree) tree;

    var ifStatement = getFirstIfStatementInCaseBody(caseGroup);
    // We do not want to inspect case groups where the body does not have an if statement or if it has also an else statement
    if (ifStatement == null || ifStatement.elseStatement() != null) {
      return;
    }
    var caseLabel = caseGroup.labels().get(0);
    if (isCaseDefaultOrNull(caseLabel)) {
      return;
    }

    var caseExpression = caseLabel.expressions().get(0);
    boolean isGuardedPattern = caseExpression instanceof GuardedPatternTree;

    QuickFixHelper.newIssue(context).forRule(this)
      .onTree(ifStatement)
      .withMessage(isGuardedPattern ? ISSUE_MESSAGE_MERGE : ISSUE_MESSAGE_REPLACE)
      .withQuickFix(() -> computeQuickFix(ifStatement, caseLabel, isGuardedPattern, context))
      .report();

  }

  private static IfStatementTree getFirstIfStatementInCaseBody(CaseGroupTree caseGroup) {
    if (!caseGroup.body().isEmpty() && caseGroup.body().get(0) instanceof BlockTree caseBlock) {
      // We need to check if the first and only element of the body is an if statement
      var blockBody = caseBlock.body();
      if (blockBody.size() == 1 && blockBody.get(0) instanceof IfStatementTree ifStatement) {
        return ifStatement;
      }
    }
    return null;
  }

  private static boolean isCaseDefaultOrNull(CaseLabelTree caseLabel) {
    return caseLabel.expressions().isEmpty() || caseLabel.expressions().get(0) instanceof NullPatternTree;
  }

  private static JavaQuickFix computeQuickFix(IfStatementTree ifStatement, CaseLabelTree caseLabel, boolean shouldMergeConditions,
    JavaFileScannerContext context) {
    var quickFixBuilder = JavaQuickFix.newQuickFix(shouldMergeConditions ? ISSUE_MESSAGE_MERGE : ISSUE_MESSAGE_REPLACE);
    String replacement;
    if (ifStatement.thenStatement() instanceof BlockTree block && !block.body().isEmpty()) {
      var firstToken = QuickFixHelper.nextToken(block.openBraceToken());
      var lastToken = QuickFixHelper.previousToken(block.closeBraceToken());
      replacement = QuickFixHelper.contentForRange(firstToken, lastToken, context);
    } else {
      replacement = QuickFixHelper.contentForTree(ifStatement.thenStatement(), context);
    }
    quickFixBuilder.addTextEdit(
      JavaTextEdit.replaceTree(ifStatement, replacement)
    );
    var replacementStringPrefix = shouldMergeConditions ? " && " : " when ";
    quickFixBuilder.addTextEdit(
      JavaTextEdit.insertBeforeTree(caseLabel.colonOrArrowToken(),
        replacementStringPrefix + QuickFixHelper.contentForTree(ifStatement.condition(), context) + " ")
    );
    QuickFixHelper.addParenthesisIfRequired(quickFixBuilder, ifStatement.condition());
    for(var expr : caseLabel.expressions()) {
      if(expr instanceof GuardedPatternTree guardedPattern) {
        QuickFixHelper.addParenthesisIfRequired(quickFixBuilder, guardedPattern.expression());
      }
    }
    return quickFixBuilder.build();
  }

}
