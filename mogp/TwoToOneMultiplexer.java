package mogp;

/**
 * TwoToOneMultiplexer is an implementation of the two-to-one
 * multiplexer problem class.
 * 
 * @author Jonathan Fieldsend
 * @version 1.0
 */
class TwoToOneMultiplexer extends Problem
{
    /**
     * Constructs an instance of the two to one multiplexer, 
     * with the corresponding input test cases and targets
     */
    TwoToOneMultiplexer()
    {
        super.variableNumber = 3;
        super.fitnessCases = 8;
        super.inputs = new boolean[super.fitnessCases][variableNumber];
        super.targets = new boolean[super.fitnessCases];
        
        super.inputs[0] = new boolean[]{true, false, false};
        super.targets[0] = false;
        super.inputs[1] = new boolean[]{false, false, false};
        super.targets[1] = false;
        super.inputs[2] = new boolean[]{true, true, false};
        super.targets[2] = true;
        super.inputs[3] = new boolean[]{false, true, false};
        super.targets[3] = true;
        super.inputs[4] = new boolean[]{true, true, true};
        super.targets[4] = true;
        super.inputs[5] = new boolean[]{true, false, true};
        super.targets[5] = true;
        super.inputs[6] = new boolean[]{false, true, true};
        super.targets[6] = false;
        super.inputs[7] = new boolean[]{false, false, true};
        super.targets[7] = false;
    }
}
