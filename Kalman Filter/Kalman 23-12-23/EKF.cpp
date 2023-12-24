#include "EKF.h"
//EXTENDED KALMAN FILTER
//In this code the gyro measurements are used for predicting the state and accelerometer data are used for the correction step
// Qq: Process noise for quaternion components
// Qw: Process noise for angular velocity components
// Rr: Measurement noise for accelerometer and magnetometer data

//Constructor
EKF::EKF(float Qq, float Qw, float Rr) {

	// Updating covariances based on initial values
	this->Q = cv::Mat::eye(7, 7, CV_32F);
	for (int i = 0; i < 4; i++)
		this->Q.at<float>(i, i) = Qq;
	for (int i = 4; i < 7; i++)
		this->Q.at<float>(i, i) = Qw;

	this->R = cv::Mat::eye(4, 4, CV_32F) * Rr;

	this->x_apriori = cv::Mat::zeros(7, 1, CV_32F); // x_k|k-1
	this->x_aposteriori = cv::Mat::zeros(7, 1, CV_32F); // x_k|k


	this->Q.copyTo(this->P_apriori);
	this->Q.copyTo(this->P_aposteriori);
	//H is the output
	this->H = cv::Mat::zeros(4, 7, CV_32F);
	for (int i = 0; i < 4; i++)
		this->H.at<float>(i, i) = 1.0;

	firstMeasurement = true;
	correctTime = true;
}
//Jacobian matrix F for the orientation quaternion model
//*w is a pointer that points to an array containing gyroscope measurements
//dt is the time interval between the current and the previous measurements
cv::Mat EKF::jacobian(float * w, float dt) {
	cv::Mat F = cv::Mat::zeros(7, 7, CV_32F);
	//w[0]=x w[1]=y, w[2]=z
	// 1st row
	F.at<float>(0, 0) = 1.0;
	F.at<float>(0, 1) = -0.5 * (w[0] - this->x_apriori.at<float>(4)) * dt;
	F.at<float>(0, 2) = -0.5 * (w[1] - this->x_apriori.at<float>(5)) * dt;
	F.at<float>(0, 3) = -0.5 * (w[2] - this->x_apriori.at<float>(6)) * dt;
	F.at<float>(0, 4) = 0.5 * dt * this->x_apriori.at<float>(1);
	F.at<float>(0, 5) = 0.5 * dt * this->x_apriori.at<float>(2);
	F.at<float>(0, 6) = 0.5 * dt * this->x_apriori.at<float>(3);

	// 2nd row
	F.at<float>(1, 0) = 0.5 * (w[0] - this->x_apriori.at<float>(4)) * dt;
	F.at<float>(1, 1) = 1;
	F.at<float>(1, 2) = 0.5 * (w[2] - this->x_apriori.at<float>(6)) * dt;
	F.at<float>(1, 3) = -0.5 * (w[1] - this->x_apriori.at<float>(5)) * dt;
	F.at<float>(1, 4) = -0.5 * dt * this->x_apriori.at<float>(0);
	F.at<float>(1, 5) = 0.5 * dt * this->x_apriori.at<float>(3);
	F.at<float>(1, 6) = -0.5 * dt * this->x_apriori.at<float>(2);

	// 3rd row
	F.at<float>(2, 0) = 0.5 * (w[1] - this->x_apriori.at<float>(5)) * dt;
	F.at<float>(2, 1) = -0.5 * (w[2] - this->x_apriori.at<float>(6)) * dt;
	F.at<float>(2, 2) = 1;
	F.at<float>(2, 3) = 0.5 * (w[0] - this->x_apriori.at<float>(4)) * dt;
	F.at<float>(2, 4) = -0.5 * dt * this->x_apriori.at<float>(3);
	F.at<float>(2, 5) = -0.5 * dt * this->x_apriori.at<float>(0);
	F.at<float>(2, 6) = 0.5 * dt * this->x_apriori.at<float>(1);

	// 4th row
	F.at<float>(3, 0) = 0.5 * (w[2] - this->x_apriori.at<float>(6)) * dt;
	F.at<float>(3, 1) = 0.5 * (w[1] - this->x_apriori.at<float>(5)) * dt;
	F.at<float>(3, 2) = -0.5 * (w[0] - this->x_apriori.at<float>(4)) * dt;
	F.at<float>(3, 3) = 1;
	F.at<float>(3, 4) = 0.5 * dt * this->x_apriori.at<float>(2);
	F.at<float>(3, 5) = -0.5 * dt * this->x_apriori.at<float>(1);
	F.at<float>(3, 6) = -0.5 * dt * this->x_apriori.at<float>(0);

	// 5th row
	F.at<float>(4, 4) = 1.0;

	// 6th row
	F.at<float>(5, 5) = 1.0;

	// 7th row
	F.at<float>(6, 6) = 1.0;

	return F;
}
//The state matrix prediction A_(k|k-1) prediction with the 
//*w is a pointer that points to an array containing gyroscope measurements
//dt is the time interval between the current and the previous measurements
v::Mat EKF::state(float * w, float dt) {

	cv::Mat F = cv::Mat::zeros(7, 1, CV_32F); //creates matrix of zeros named F

	F.at<float>(4) = this->x_aposteriori.at<float>(4);
	F.at<float>(5) = this->x_aposteriori.at<float>(5);
	F.at<float>(6) = this->x_aposteriori.at<float>(6);
	//state matrix A
	cv::Mat A = cv::Mat::zeros(4, 4, CV_32F);
	// A 1st row
	A.at<float>(0, 0) = 1.0;
	A.at<float>(0, 1) = -0.5 * (w[0] - F.at<float>(4)) * dt;
	A.at<float>(0, 2) = -0.5 * (w[1] - F.at<float>(5)) * dt;
	A.at<float>(0, 3) = -0.5 * (w[2] - F.at<float>(6)) * dt;

	// A 2nd row
	A.at<float>(1, 0) = 0.5 * (w[0] - F.at<float>(4)) * dt;
	A.at<float>(1, 1) = 1;
	A.at<float>(1, 2) = 0.5 * (w[2] - F.at<float>(6)) * dt;
	A.at<float>(1, 3) = -0.5 * (w[1] - F.at<float>(5)) * dt;

	// A 3rd row
	A.at<float>(2, 0) = 0.5 * (w[1] - F.at<float>(5)) * dt;
	A.at<float>(2, 1) = -0.5 * (w[2] - F.at<float>(6)) * dt;
	A.at<float>(2, 2) = 1;
	A.at<float>(2, 3) = 0.5 * (w[0] - F.at<float>(4)) * dt;

	// A 4th row
	A.at<float>(3, 0) = 0.5 * (w[2] - F.at<float>(6)) * dt;
	A.at<float>(3, 1) = 0.5 * (w[1] - F.at<float>(5)) * dt;
	A.at<float>(3, 2) = -0.5 * (w[0] - F.at<float>(4)) * dt;
	A.at<float>(3, 3) = 1;

	// Only (1:4)
	cv::Mat x(this->x_aposteriori, cv::Rect(0, 0, 1, 4));
	x = A * x;

	for (int i = 0; i < 4; i++)
		F.at<float>(i) = x.at<float>(i);

	return F; //returns the jacobian matrix
}

//Gyro measurements are used to  update the x_apiori (x_k|k-1)
//*inputArray is a pointer that points to an array containing gyroscope measurements(angular velocities)
//dt is the time interval between the current and the previous measurements
//*currentEstimate is a pointer to the current state estimate(output)
void EKF::predict(float * inputArray, float _dt, float *currentEstimate) {
if (!correctTime) {
		this->x_apriori = this->state(inputArray, _dt);
		cv::Mat F = this->jacobian(inputArray, _dt);
		this->P_apriori = F * this->P_aposteriori * F.t() + this->Q;
		correctTime = true;
	}
	for (int i = 0; i < 4; i++)
		currentEstimate[i] = this->x_aposteriori.at<float>(i); // update the current state estimate

}


//In this method the accelerometer and magnetometer data used for the correction step
// and Kalman Gain Calculation to update the x_aposteriori (x_k|k)

//addrZ is a pointer that point to the accelerometer and magnetometer measurements
//*currentEstimate is a pointer to the current state estimate(input)

void EKF::correct(float * addrZ, float *currentEstimate) {

	// Converting measurement
	cv::Mat z = cv::Mat::eye(4, 1, CV_32F);
	for (int i = 0; i < 4; i++)
		z.at<float>(i) = addrZ[i];

	if (firstMeasurement) {
		firstMeasurement = false;
		correctTime = false;
		for (int i = 0; i < 4; i++) {
			this->x_aposteriori.at<float>(i) = z.at<float>(i);
		}
	} else if (correctTime) {
		correctTime = false;

		// Additional variables
		cv::Mat I = cv::Mat::eye(7, 7, CV_32F); // Identity matrix
		cv::Mat K = cv::Mat::zeros(7, 4, CV_32F); // Kalman Gain
		//Updates the predicted state x_apriori (x_k|k-1) using Kalman Gain and the difference between the actual measurement (z)
		// and the predicted measurement (H*x_k | k-1)
		K = (this->P_apriori * this->H.t()) * (this->H * this->P_apriori * this->H.t() + this->R).inv();
				
		this->x_aposteriori = this->x_apriori + K * (z - this->H * this->x_apriori);
		//P_aposteriori (P_k|k) update using the Kalman Gain
		this->P_aposteriori = (I - K * this->H) * this->P_apriori;
	}
	//Copying the current state estimate to the currentEstimate array
	for (int i = 0; i < 4; i++)
		currentEstimate[i] = this->x_aposteriori.at<float>(i);
}

