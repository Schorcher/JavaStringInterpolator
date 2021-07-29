package com.github.schorcher.stringInterpolator;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Token {

    private final String value;
    private final TokenType tokenType;
    private final int offset;

}
