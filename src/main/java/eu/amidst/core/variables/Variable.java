package eu.amidst.core.variables;

import eu.amidst.core.database.Attribute;

/**
 * Created by afa on 02/07/14.
 */
public interface Variable {

    public String getName();

    public int getVarID();

    public boolean isObservable();

    public int getNumberOfStates();

    public StateSpaceType getStateSpaceType();

    public DistType getDistributionType();

    public boolean isTemporalClone();

    public boolean isDynamicVariable();

    public Attribute getAttribute();

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object o);

}
