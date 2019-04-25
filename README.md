# gecco_2015_mogp

Codebase for GECCO 2015 paper Strength Through Diversity: Disaggregation and Multi-Objectivisation 
Approaches for Genetic Programming by Jonathan Fieldsend and Alberto Moraglio 

Paper available at the links below:
https://ore.exeter.ac.uk/repository/handle/10871/17041
https://doi.org/10.1145/2739480.2754643

Class to run is the BooleanGPO class in the mogp package. You will need to compile the source of the package first.

When running, arguments need to be provided at the command line. If these are ommitted some advice is provided, i.e., if the command line prompt is indicted with prompt]

prompt] java mogp.BooleanGP

Insufficient arguments, requires: maintenence type (R, B, BP, F, FP, S, SP, D or DP) problem type (2, 4 or 8) population size (postive integer) fold start number fold end number

Please refer to the documentation for the range of problem type arguments (and their interpretation)

A run of the DP maintence approach across 5 folds, on problem type 2, with a search population of 100,  would therefore be invoked with:  

prompt] java mogp.BooleanGP DP 2 100 1 5

FOLD: 1
Pareto set size: 10
Pareto set size: 9
..
..
..
FOLD: 5
Pareto set size: 9
Pareto set size: 8
Pareto set size: 9
Pareto set size: 10
Pareto set size: 10
Pareto set size: 8
Pareto set size: 9
Pareto set size: 9
Pareto set size: 9
Pareto set size: 7
Pareto set size: 5
Pareto set size: 6
Pareto set size: 6
Pareto set size: 7
Pareto set size: 1

total time: 54.5680000000047 milli seconds

calls: 18800 times

Av time per gen: 0.002902553191489612 milliseconds

total time: 734.833 milliseconds

Percentage total: 7.425904933502538%

Details printed to the screen for this approach include the number of nondominated members at each generation, and at the end of the run the total number of calls to the Pareto set maintenance routines, and the percentage of the run time spent on this (note there are more efficient regimes available for preserving non-dominated sets than those implemented here, as referred to in the original manuscript).

Additionally files tracking quality will be written out, specifically in the example here

bool_gecco2015_typeDP_problem2_pop100_fold1_results.txt	
bool_gecco2015_typeDP_problem2_pop100_fold2_results.txt	
bool_gecco2015_typeDP_problem2_pop100_fold3_results.txt	
bool_gecco2015_typeDP_problem2_pop100_fold4_results.txt	
bool_gecco2015_typeDP_problem2_pop100_fold5_results.txt
timing_bool_gecco2015_typeDP_problem2_pop100_evals.txt
