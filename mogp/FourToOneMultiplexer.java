package mogp;


/**
 * FourToOneMultiplexer is an implementation of the four to one
 * multiplexer problem class.
 * 
 * @author Jonathan Fieldsend
 * @version 1.0
 */
class FourToOneMultiplexer extends Problem
{
    /**
     * Constructs an instance of the four to one multiplexer, 
     * with the corresponding input test cases and targets
     */
    FourToOneMultiplexer()
    {
        super.variableNumber = 6;
        super.fitnessCases = 64;
        super.inputs = new boolean[super.fitnessCases][super.variableNumber];
        super.targets = new boolean[super.fitnessCases];
      
        int index = 0;
        for (int d1=0; d1<2; d1++) { // input 1
            for (int d2=0; d2<2; d2++) { // input 2
                for (int d3=0; d3<2; d3++) { // input 3
                    for (int d4=0; d4<2; d4++) { // input 4
                        for (int a1=0; a1<2; a1++) { // selector 1
                            for (int a2=0; a2<2; a2++) { // selector 2
                                inputs[index][0] = (d1>0);
                                inputs[index][1] = (d2>0);
                                inputs[index][2] = (d3>0);
                                inputs[index][3] = (d4>0);
                                inputs[index][4] = (a1>0);
                                inputs[index][5] = (a2>0);
                                if ((inputs[index][4]==false) && (inputs[index][5]==false))
                                    targets[index] = inputs[index][0];
                                else if ((inputs[index][4]==false) && (inputs[index][5]))
                                    targets[index] = inputs[index][1];
                                else if ((inputs[index][4]) && (inputs[index][5]==false))
                                    targets[index] = inputs[index][2];
                                else if ((inputs[index][4]) && (inputs[index][5]))
                                    targets[index] = inputs[index][3];
                                
                                index++;
                            }
                        }
                    }
                }
            }
        }
    }
    
}
