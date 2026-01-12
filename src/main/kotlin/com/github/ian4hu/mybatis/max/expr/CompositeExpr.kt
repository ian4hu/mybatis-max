/*
 *    Copyright 2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.ian4hu.mybatis.max.expr

import com.github.ian4hu.mybatis.max.Expr

/**
 * Marker interface for composite expressions that may need parentheses when nested.
 *
 * Composite expressions include logical operations (AND, OR, NOT) and other complex
 * expressions that require grouping in certain contexts to maintain correct precedence.
 */
interface CompositeExpr : Expr
