package network;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Neuralnet {
	protected double[][] neuron;				// Holds each neuron's value in the network

	private double[][] neuInput;			// Stores the input to each neuron

	protected double[][][] weight;			// Represents the synappse connections between the neurons in the network
	private double[][][] preWeightDelta;	// Stores the previous adjustment made to the weights to aid in implementation of momentum

	protected double[][] bias;				// Allows greater flexibiltiy of the transfer funcitons within neurons
	private double[][] preBiasDelta;		// Stores the previous adjustment made to the baises to aid in implementation of momentum

	private double[][] delta;				// Change in weight's value to minimise error

	private double randRange = 1.0;			// Range from which to randomly set inital weights
	
	public double fitness = 0;
	
	public Neuralnet(int inputDim, int hiddenDim, int outputDim){		// Constructor that assumes single hidden layer network

		// Initializing network's dimentions and neurons
		neuron = new double[3][];
		neuron[0] = new double[inputDim];
		neuron[1] = new double[hiddenDim];
		neuron[2] = new double[outputDim];

		delta = new double[2][];
		delta[0] = new double[hiddenDim];
		delta[1] = new double[outputDim];
		for(int x=0; x<2; x++)
			for(int y=0; y<neuron[x+1].length; y++)
				delta[x][y] = 0.0;

		neuInput = new double[3][];
		neuInput[0] = new double[inputDim];
		neuInput[1] = new double[hiddenDim];
		neuInput[2] = new double[outputDim];

		for(int x=0; x<3; x++)								// Initalizing each neuron's value to 0
			for(int y=0; y<neuron[x].length; y++){
				neuron[x][y] = 0.0;
				neuInput[x][y] = 0.0;
			}

		// Initializing network's weights
		weight = new double[2][][];
		weight[0] = new double[inputDim][hiddenDim];
		weight[1] = new double[hiddenDim][outputDim];

		preWeightDelta = new double[2][][];
		preWeightDelta[0] = new double[inputDim][hiddenDim];
		preWeightDelta[1] = new double[hiddenDim][outputDim];

		for(int x=0; x<2; x++)								// Initalizing each weight's and delta's value
			for(int y=0; y<weight[x].length; y++)
				for(int z=0; z<weight[x][y].length; z++){
					weight[x][y][z] = rand(-randRange,randRange);
					preWeightDelta[x][y][z] = 0.0;
				}

		// Initializing network's biases
		bias = new double[2][];
		bias[0] = new double[hiddenDim];
		bias[1] = new double[outputDim];

		preBiasDelta = new double[2][];
		preBiasDelta[0] = new double[hiddenDim];
		preBiasDelta[1] = new double[outputDim];

		for(int x=0; x<2; x++)								// Initalizing each bias's value
			for(int y=0; y<bias[x].length; y++){
				bias[x][y] = rand(-randRange,randRange);
				preBiasDelta[x][y] = 0.0;
			}
	}
	
	public Neuralnet(int[] lay){		// Constructor to create network with specified dimentions and layers
		int netSize = lay.length;
		// Initializing network's dimentions and neurons
		neuron = new double[netSize][];
		neuInput = new double[netSize][];

		for(int x=0; x<netSize; x++){
			neuron[x] = new double[lay[x]];
			neuInput[x] = new double[lay[x]];
		}

		delta = new double[netSize-1][];
		for(int x=1; x<netSize; x++)
			delta[x-1] = new double[lay[x]];

		for(int x=0; x<delta.length; x++)
			for(int y=0; y<neuron[x+1].length; y++)
				delta[x][y] = 0.0;		

		for(int x=0; x<netSize; x++)								// Initalizing each neuron's value to 0
			for(int y=0; y<neuron[x].length; y++){
				neuron[x][y] = 0.0;
				neuInput[x][y] = 0.0;
			}

		// Initializing network's weights
		weight = new double[netSize-1][][];
		preWeightDelta = new double[netSize-1][][];

		for(int x=0; x<netSize-1; x++){
			weight[x] = new double[lay[x]][lay[x+1]];
			preWeightDelta[x] = new double[lay[x]][lay[x+1]];
		}

		for(int x=0; x<netSize-1; x++)								// Initalizing each weight's and delta's value
			for(int y=0; y<weight[x].length; y++)
				for(int z=0; z<weight[x][y].length; z++){
					weight[x][y][z] = rand(-randRange,randRange);
					preWeightDelta[x][y][z] = 0.0;
				}

		// Initializing network's biases
		bias = new double[netSize-1][];
		preBiasDelta = new double[netSize-1][];

		for(int x=1; x<netSize; x++){
			bias[x-1] = new double[lay[x]];
			preBiasDelta[x-1] = new double[lay[x]];
		}

		for(int x=0; x<netSize-1; x++)								// Initalizing each bias's value
			for(int y=0; y<bias[x].length; y++){
				bias[x][y] = rand(-randRange,randRange);
				preBiasDelta[x][y] = 0.0;
			}
	}

	public Neuralnet(String filePath){	// Constructor to create network with weight and bias values in provided file

		BufferedReader reader = null;
		try{
			InputStream is = getClass().getResourceAsStream(filePath);
			reader = new BufferedReader(new InputStreamReader(is));
			int netSize = Integer.parseInt(reader.readLine());

			int[] lay = new int[netSize];
			for(int x=0; x<lay.length; x++)
				lay[x] = Integer.parseInt(reader.readLine());

			// Initializing network's dimentions and neurons
			neuron = new double[netSize][];
			neuInput = new double[netSize][];

			for(int x=0; x<netSize; x++){
				neuron[x] = new double[lay[x]];
				neuInput[x] = new double[lay[x]];
			}

			delta = new double[netSize-1][];
			for(int x=1; x<netSize; x++)
				delta[x-1] = new double[lay[x]];

			for(int x=0; x<delta.length; x++)
				for(int y=0; y<neuron[x+1].length; y++)
					delta[x][y] = 0.0;		

			for(int x=0; x<netSize; x++)								// Initalizing each neuron's value to 0
				for(int y=0; y<neuron[x].length; y++){
					neuron[x][y] = 0.0;
					neuInput[x][y] = 0.0;
				}

			// Initializing network's weights
			weight = new double[netSize-1][][];
			preWeightDelta = new double[netSize-1][][];

			for(int x=0; x<netSize-1; x++){
				weight[x] = new double[lay[x]][lay[x+1]];
				preWeightDelta[x] = new double[lay[x]][lay[x+1]];
			}

			for(int x=0; x<netSize-1; x++)								// Initalizing each weight's and delta's value
				for(int y=0; y<weight[x].length; y++)
					for(int z=0; z<weight[x][y].length; z++){
						weight[x][y][z] = Double.parseDouble(reader.readLine());	// reading weight from file
						preWeightDelta[x][y][z] = 0.0;
					}

			// Initializing network's biases
			bias = new double[netSize-1][];
			preBiasDelta = new double[netSize-1][];

			for(int x=1; x<netSize; x++){
				bias[x-1] = new double[lay[x]];
				preBiasDelta[x-1] = new double[lay[x]];
			}

			for(int x=0; x<netSize-1; x++)								// Initalizing each bias's value
				for(int y=0; y<bias[x].length; y++){
					bias[x][y] = Double.parseDouble(reader.readLine());		// reading bias from file
					preBiasDelta[x][y] = 0.0;
				}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(reader!=null)
					reader.close();
			}catch(Exception e){}
		}
	}
	
	public Neuralnet(Neuralnet a, Neuralnet b){
		/*
		 * Constructor which requires two established Neural Networks with identical toplogies
		 * Constructs a hybrid network with weights averaged from two parent networks, with a small chance of mutation
		 * that displaces the weight by a constant factor.
		 * Used for the purpose of NeuralEvoloution to breed two parents to create a child Network.
		 * 
		 */
		
		if(a.neuron.length != b.neuron.length)
			throw new IllegalArgumentException("Incompatable Topology Structure Exception!");
		for(int layer = 0; layer<a.neuron.length; layer++)
			if(a.neuron[layer].length != b.neuron[layer].length)
				throw new IllegalArgumentException("Incompatable Topology Structure Exception");
		
		int inputDim = a.neuron[0].length;
		int hiddenDim = a.neuron[1].length;
		int outputDim = a.neuron[2].length;
		// Initializing network's dimentions and neurons
		neuron = new double[3][];
		neuron[0] = new double[inputDim];
		neuron[1] = new double[hiddenDim];
		neuron[2] = new double[outputDim];

		delta = new double[2][];
		delta[0] = new double[hiddenDim];
		delta[1] = new double[outputDim];
		for(int x=0; x<2; x++)
			for(int y=0; y<neuron[x+1].length; y++)
				delta[x][y] = 0.0;

		neuInput = new double[3][];
		neuInput[0] = new double[inputDim];
		neuInput[1] = new double[hiddenDim];
		neuInput[2] = new double[outputDim];

		for(int x=0; x<3; x++)								// Initalizing each neuron's value to 0
			for(int y=0; y<neuron[x].length; y++){
				neuron[x][y] = 0.0;
				neuInput[x][y] = 0.0;
			}

		// Initializing network's weights
		weight = new double[2][][];
		weight[0] = new double[inputDim][hiddenDim];
		weight[1] = new double[hiddenDim][outputDim];

		preWeightDelta = new double[2][][];
		preWeightDelta[0] = new double[inputDim][hiddenDim];
		preWeightDelta[1] = new double[hiddenDim][outputDim];

		for(int x=0; x<2; x++)								// Initalizing each weight's and delta's value
			for(int y=0; y<weight[x].length; y++)
				for(int z=0; z<weight[x][y].length; z++){
					double newWeight = ave(a.weight[x][y][z],b.weight[x][y][z]);
					if(Math.random()<0.1){
						if(Math.random()<0.5)
							newWeight += newWeight*Math.random()*2; 
						else
							newWeight -= newWeight*Math.random()*2; 
					}
					weight[x][y][z] = newWeight;
					preWeightDelta[x][y][z] = 0.0;
				}

		// Initializing network's biases
		bias = new double[2][];
		bias[0] = new double[hiddenDim];
		bias[1] = new double[outputDim];

		preBiasDelta = new double[2][];
		preBiasDelta[0] = new double[hiddenDim];
		preBiasDelta[1] = new double[outputDim];

		for(int x=0; x<2; x++)								// Initalizing each bias's value
			for(int y=0; y<bias[x].length; y++){
				double newWeight = ave(a.bias[x][y],b.bias[x][y]);
				if(Math.random()<0.1){
					if(Math.random()<0.5)
						newWeight += newWeight*Math.random()*2; 
					else
						newWeight -= newWeight*Math.random()*2; 
				}
				bias[x][y] = newWeight;
				preBiasDelta[x][y] = 0.0;
			}
	}

	public double Learn(double[] inputs, double[] expected, double trainRate, double momentum){		// Backpropagation
		if(inputs.length != neuron[0].length)
			throw new IllegalArgumentException("Invalid input size!");
		if(expected.length != neuron[neuron.length-1].length)
			throw new IllegalArgumentException("Invalid desired output size!");

		// Init variables
		double error = 0.0, sum = 0.0, wDelta = 0.0, bDelta = 0.0;

		// Evaluate network with input to get output
		Evaluate(inputs);

		// Back-propogate error

		// Calculate Output layer deltas
		int outlay = neuron.length -1;
		for(int x=0; x<neuron[outlay].length; x++){
			delta[outlay-1][x] = neuron[outlay][x] - expected[x];
			error += Math.pow(delta[outlay-1][x], 2);
			delta[outlay-1][x] *= 1;//sigmoid_derivitive(neuron[outlay][x]);
		}

		// Calculate Hidden layer deltas
		for(int lay = neuron.length-2; lay>0; lay--)
			for(int i = 0; i<neuron[lay].length; i++){
				sum = 0.0;

				for(int x=0; x<neuron[lay+1].length; x++)		// sum of deltas of next layer neurons * weights to them from current neuron
					sum += weight[lay][i][x] * delta[lay][x];			

				sum *= sigmoid_derivitive(neuron[lay][i]);

				delta[lay-1][i] = sum;
			}

		//    Update weights of network 
		for(int lay = neuron.length-1; lay>0; lay--)
			for(int a=0; a<neuron[lay-1].length; a++)
				for(int b=0; b<neuron[lay].length; b++){
					wDelta = trainRate * delta[lay-1][b]
							* neuron[lay-1][a]
							+ momentum * preWeightDelta[lay-1][a][b];
					weight[lay-1][a][b] -= wDelta;
					preWeightDelta[lay-1][a][b] = wDelta;
				}

		//    Update biases of network
		for(int lay=neuron.length-1; lay>0; lay--)
			for(int x=0; x<neuron[lay].length; x++)
				for(int y=0; y<bias[lay-1].length; y++){
					bDelta = trainRate * delta[lay-1][x]
							+ momentum * preBiasDelta[lay-1][y];
					bias[lay-1][y] -= bDelta;
					preBiasDelta[lay-1][y] = bDelta;
				}

		return error;
	}

	public void Evaluate(double[] inputs){		// Forward propagation to evalutate array of inputs 
		if(inputs.length != neuron[0].length)
			throw new IllegalArgumentException("Invalid input size!");

		for(int x=0; x<neuron[0].length; x++)								// copy inputs into input neurons
			neuron[0][x]= inputs[x];

		for(int layer = 1; layer<neuron.length; layer++)					// calculate inputs and values of hidden and output neurons
			for(int neu = 0; neu<neuron[layer].length; neu++){
				double sum = 0.0;
				for(int preNeu = 0; preNeu < neuron[layer-1].length; preNeu++)	// sum inputs from previous neurons
					sum += neuron[layer-1][preNeu] * weight[layer-1][preNeu][neu];
				sum += bias[layer-1][neu];				// add bias to neuron
				neuInput[layer][neu] = sum;						// store current sum as input into neuron
				neuron[layer][neu] = layer==neuron.length-1?sum:sigmoid(sum);		// calculate neuron's value by running input through transfer function
			}
	}

	public double[] getOutput(){					// returns outputs of neural network
		return neuron[neuron.length-1];
	}

	public void dispNet(){								// display weights and biases of the network
		// display input weights
		System.out.print("Input weights: ");
		for(int x=0; x<weight[0].length; x++){
			for(int y=0; y<weight[0][x].length; y++)
				System.out.print(weight[0][x][y]+", ");
			System.out.print("|");
		}
		System.out.println();

		// display hidden weights
		System.out.print("Hidden weights: ");
		for(int x=1; x<weight.length; x++){
			for(int y=0; y<weight[x].length; y++){
				for(int z=0; z<weight[x][y].length; z++)
					System.out.print(weight[x][y][z]+", ");
				System.out.print("|");
			}
			System.out.println();
		}

		//display biases for each node
		System.out.print("Biases: ");
		for(int x=0; x<bias.length; x++){
			for(int y=0; y<bias[x].length; y++)
				System.out.print(bias[x][y]+", ");
			System.out.println();
		}
	}

	public void Savenet(String filename){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			int netSize = neuron.length;							// Writing network size and layer sizes to file
			out.write(String.valueOf(netSize));
			out.newLine();

			for(int x=0; x<neuron.length; x++){
				out.write(String.valueOf(neuron[x].length));
				out.newLine();
			}

			for(int x=0; x<netSize-1; x++)								// Writing each weight's value to file
				for(int y=0; y<weight[x].length; y++)
					for(int z=0; z<weight[x][y].length; z++){
						out.write(String.valueOf(weight[x][y][z]));
						out.newLine();
					}

			for(int x=0; x<netSize-1; x++)								// Writing each bias's value to file
				for(int y=0; y<bias[x].length; y++){
					out.write(String.valueOf(bias[x][y]));
					if(x != netSize-2 || y != bias[x].length-1)
					out.newLine();
				}

			out.close();
		} catch (Exception e) {e.printStackTrace();}
	}

	private static double sigmoid(double x){
		return 1 / ( 1 + Math.exp(-x));
	}
	private static double sigmoid_derivitive(double x){
		return x * (1 - x);
	}
	private static double rand(double d, double e){
		return  (d + (Math.random()*((e-d)+1)));
	}
	private static int rand(int d, int e){
		return  (int) (d + (Math.random()*((e-d)+1)));
	}
	private double ave(double a, double b){
		return (a+b)/2;
	}
}