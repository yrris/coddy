package com.yrris.coddy.ai.parser;

public interface CodeParser<T> {

    T parseCode(String codeContent);
}
