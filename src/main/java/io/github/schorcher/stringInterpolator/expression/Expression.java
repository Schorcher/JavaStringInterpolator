package io.github.schorcher.stringInterpolator.expression;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Expression {

    private final String value;
    private final ExpType expType;
    private final int offset;

}
