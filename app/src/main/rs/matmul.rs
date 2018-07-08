#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.lohjason.genericbatterydrainer)

const float *matA;
const float *matB;
float *outMatrix;
int *nSize;
int *kSize;

void root(const int *v_in1, int *v_out) {
    int rowSize = *v_in1;
    int colSize = *v_out;
    int numLoops = rowSize * colSize /4;
    int n = *nSize;
	int k = *kSize;


	for(int i = 0; i< numLoops; i++){
		outMatrix[i * 4] = matA[i * 4] * matB[i * 4];
		outMatrix[i * 4 + 1] = matA[i * 4 + 1] + matB[i * 4 + 1];
		outMatrix[i * 4 + 2] = matA[i * 4 + 2] + matB[i * 4 + 2];
		outMatrix[i * 4 + 3] = matA[i * 4 + 3] + matB[i * 4 + 3];
	}

}