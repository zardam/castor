/*
 * Copyright 2008 Udai Gupta, Ralf Joachim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.castor.cpa.query.object.literal;

import org.castor.cpa.query.Expression;
import org.castor.cpa.query.Literal;
import org.castor.cpa.query.QueryObject;
import org.castor.cpa.query.object.expression.AbstractExpression;

import junit.framework.TestCase;

/**
 * Junit Test for testing EnumLiteral class.
 * 
 * @author <a href="mailto:mailtoud AT gmail DOT com">Udai Gupta</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 7121 $ $Date: 2006-04-25 16:09:10 -0600 (Tue, 25 Apr 2006) $
 * @since 1.3
 */
public final class TestEnumLiteral extends TestCase {
    //--------------------------------------------------------------------------

    public TestEnumLiteral(final String name) {
        super(name);
    }

    //--------------------------------------------------------------------------

    /**
     * Junit Test for instance Enum Literal.
     */
    public void testInstance() {
        QueryObject n = new EnumLiteral(MockEnum.TEST1);
        assertTrue(n instanceof AbstractLiteral);
        assertTrue(n instanceof Literal);
        assertTrue(n instanceof AbstractExpression);
        assertTrue(n instanceof Expression);
    }
    
    /**
     * Junit Test for constructor and getter.
     */
    public void testConstructor() {
        // TODO test EnumLiteral(String) constructor
        // in addition to the success case also use parameters that
        // reproduce each fail situation

        // TODO test passing null to EnumLiteral(Enum) constructor
        // see StringLiteral as an example
        
        EnumLiteral n = new EnumLiteral(MockEnum.TEST1);
        assertEquals(MockEnum.TEST1, n.getValue());
    }
    
    /**
     * Junit Test for Enum Literal toString method.
     */
    public void testToString() {
        EnumLiteral n = new EnumLiteral(MockEnum.TEST1);
        assertEquals("org.castor.cpa.query.object.literal.MockEnum.TEST1", n.toString()); 
    }

    //--------------------------------------------------------------------------
}
