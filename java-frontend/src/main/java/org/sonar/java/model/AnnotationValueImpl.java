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
package org.sonar.java.model;

import org.sonar.plugins.java.api.semantic.SymbolMetadata;

public class AnnotationValueImpl implements SymbolMetadata.AnnotationValue {

  private final String name;
  private final Object value;

  public AnnotationValueImpl(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public Object value() {
    return value;
  }

  @Override
  public String name() {
    return name;
  }

}
