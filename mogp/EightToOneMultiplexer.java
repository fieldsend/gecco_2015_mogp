package mogp;


/**
 * EightToOneMultiplexer is an implementation of the eight to one
 * multiplexer problem class.
 * 
 * @author Jonathan Fieldsend
 * @version 1.0
 */
class EightToOneMultiplexer extends Problem
{
    /**
     * Constructs an instance of the eight to one multiplexer, 
     * with the corresponding input test cases and targets
     */
    EightToOneMultiplexer()
    {
        super.variableNumber = 11;
        super.fitnessCases = 2048;
        super.inputs = new boolean[super.fitnessCases][super.variableNumber];
        super.targets = new boolean[super.fitnessCases];
      
        int index = 0;
        for (int a1=0; a1<2; a1++) { // input 1
            for (int a2=0; a2<2; a2++) { // input 2
                for (int a3=0; a3<2; a3++) { // input 3
                    for (int a4=0; a4<2; a4++) { // input 4
                        for (int a5=0; a5<2; a5++) { // input 5
                            for (int a6=0; a6<2; a6++) { // input 6
                                for (int a7=0; a7<2; a7++) { // input 7
                                    for (int a8=0; a8<2; a8++) { // input 8
                                        for (int d1=0; d1<2; d1++) { // selector 1
                                            for (int d2=0; d2<2; d2++) { // selector 2
                                                for (int d3=0; d3<2; d3++) { // selector 3
                                                    inputs[index][0] = (a1>0);
                                                    inputs[index][1] = (a2>0);
                                                    inputs[index][2] = (a3>0);
                                                    inputs[index][3] = (a4>0);
                                                    inputs[index][4] = (a5>0);
                                                    inputs[index][5] = (a6>0);
                                                    inputs[index][6] = (a7>0);
                                                    inputs[index][7] = (a8>0);
                                                    inputs[index][8] = (d1>0);
                                                    inputs[index][9] = (d2>0);
                                                    inputs[index][10] = (d3>0);
                                                    
                                                    if ((inputs[index][8]==false) && (inputs[index][9]==false) && (inputs[index][10]==false))
                                                        targets[index] = inputs[index][0];
                                                    else if ((inputs[index][8]==false) && (inputs[index][9]==false) && (inputs[index][10]))
                                                        targets[index] = inputs[index][1];
                                                    else if ((inputs[index][8]==false) && (inputs[index][9]) && (inputs[index][10]==false))
                                                        targets[index] = inputs[index][2];
                                                    else if ((inputs[index][8]==false) && (inputs[index][9]) && (inputs[index][10]))
                                                        targets[index] = inputs[index][3];
                                                    else if ((inputs[index][8]) && (inputs[index][9]==false) && (inputs[index][10]==false))
                                                        targets[index] = inputs[index][4];
                                                    else if ((inputs[index][8]) && (inputs[index][9]==false) && (inputs[index][10]))
                                                        targets[index] = inputs[index][5];
                                                    else if ((inputs[index][8]) && (inputs[index][9]) && (inputs[index][10]==false))
                                                        targets[index] = inputs[index][6];
                                                    else if ((inputs[index][8]) && (inputs[index][9]) && (inputs[index][10]))
                                                        targets[index] = inputs[index][7];
                                                    
                                                    index++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
