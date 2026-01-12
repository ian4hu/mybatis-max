package com.github.ian4hu.mybatis.max.expr

import com.github.ian4hu.mybatis.max.Expr

/** A composite expression, which need be bracketed in some situation */
interface CompositeExpr<T> : Expr<T>