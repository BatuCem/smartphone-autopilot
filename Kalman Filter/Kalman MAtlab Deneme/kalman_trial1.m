%This is a kalman trial again :(

clc
close all

%% DATA EXTRACTING
fileID = fopen('sensor_data_output.txt','r');
% AccX_raw AccY_raw AccZ_raw GyroX_raw GyroY_raw GyroZ_raw MagX_raw
% MagY_raw MagZ_raw Time(ms) in format
DATA = fscanf(fileID,'%f',[10 Inf]); 
N = size(DATA); 
%length of DATA 
Nsamples = N(2); %N(2) gets the second component of [rows columns] to obtain the sample number 
EulerSaved = zeros(Nsamples, 3); %Creates a matrix of zeros for euler angles

%% INITIALIZING
Gyro_Compen_k = 30; %The first 30 gyro values will be used for compensation 
Mag_Compen_k = 775; %The fist 1000 magnometer values will be used for compensation
ref_mag = 30; %
%DATA_SI = (size(DATA));
N_Q = 1;
N_R = 100;
N_P = 1;

%% Gyroscope Compensation
%Bias
for k = 1:Gyro_Compen_k
    Bias_GyroX=mean(DATA(4,k));
    Bias_GyroY=mean(DATA(5,k));
    Bias_GyroZ=mean(DATA(6,k));
end

Bias_Gyro = [Bias_GyroX Bias_GyroY Bias_GyroZ];
%Gyro values after the compensation
for k = 1:Nsamples-1
    DATA(4,k)=DATA(4,k)-Bias_GyroX;
    DATA(5,k)=DATA(5,k)-Bias_GyroY;
    DATA(6,k)=DATA(6,k)-Bias_GyroZ;
end

%% Magnetometer Compensation using least square method
%For loop for compensation 
for k = 1:Mag_Compen_k
    Y(k,:) = [DATA(7,k)^2+DATA(8,k)^2+DATA(9,k)^2];
    X(k,:) = [DATA(7,k) DATA(8,k) DATA(9,k) 1];
end

N_X = [size(X)]; %Creates a matrix with the size of X
Xsamples = N_X(2);
Bias_Mag = 0.5*((X'*X)\eye(Xsamples))*X'*Y; %Calculates the magnetometer bias
%Magnetometer values
for k = 1:Nsamples-1
    DATA(7,k) = DATA(7,k) - Bias_Mag(1);
    DATA(8,k) = DATA(8,k) - Bias_Mag(2);
    DATA(9,k) = DATA(9,k) - Bias_Mag(3);
end

%% Set Reference Magnetic vector (NORMALIZATION)
%This calculation is for the reference vectors
%The magnitude of the original magnetic field vector
M=sqrt(DATA(7,ref_mag)^2+DATA(8,ref_mag)^2+DATA(9,ref_mag)^2);
%magnetic field unit vector
B=[DATA(7,ref_mag)/M DATA(8,ref_mag)/M DATA(9,ref_mag)/M]; 

%% EKF Algorithm
for k = 1:Nsamples-1
  %Accelerometer data
  ax=DATA(1,k);
  ay=DATA(2,k);
  az=DATA(3,k);
  %Gyro data
  p=DATA(4,k);
  q=DATA(5,k);
  r=DATA(6,k);
  %magnetometer data
  mx=DATA(7,k);
  my=DATA(8,k);
  mz=DATA(9,k);
  %Time
  dt=(DATA(10,k+1)-DATA(10,k));
  %Normalization
  G=sqrt(ax^2+ay^2+az^2);
  M=sqrt(mx^2+my^2+mz^2);
  ax=ax/G; ay=ay/G; az=az/G; mx=mx/M; my=my/M; mz=mz/M; %normalization for accelerometer and magnometer values
  %MAIN EKF FUNCTION
  [q0, q1, q2, q3] = EKF(p, q, r, B, mx, my, mz, ax, ay, az, dt, N_Q, N_R, N_P); %will return quaternion values
  %Conversion to Euler angle
  phi   =  atan2( 2*(q2*q3 + q0*q1), 1 - 2*(q1^2 + q2^2) );
  theta = -asin(  2*(q1*q3 - q0*q2) );
  psi   =  atan2( 2*(q1*q2 + q0*q3), 1 - 2*(q2^2 + q3^2) );
  EulerSaved(k, :) = [ phi theta psi ];
end 

%% Plot
%Radian to Degree
PhiSaved   = EulerSaved(:, 1) * 180/pi;
ThetaSaved = EulerSaved(:, 2) * 180/pi;
PsiSaved   = EulerSaved(:, 3) * 180/pi;
x = [0 0];
y = [-1000,1000];

figure()
P1=plot(DATA(10,:), PhiSaved, 'r');
hold on
P2=plot(DATA(10,:), ThetaSaved, 'b');
P3=plot(DATA(10,:), PsiSaved, 'g');
refline([0 0])
title('Euler Angle (degree)')
Timeline_1 = line('XData',x,'YData',y);
TimeValue_1= xlabel('');
legend([P1 P2 P3],{'Phi', 'Theta', 'Psi'},'Location','northwest','AutoUpdate','off');
axis([0 DATA(10,Nsamples) -300 300])

figure()
subplot(1,3,1)
plot(DATA(10,:),DATA(1,:),'r',DATA(10,:),DATA(2,:),'g',DATA(10,:),DATA(3,:),'b');
refline([0 0])
title('Acceleration (m/s^2)');
Timeline_2 = line('XData',x,'YData',y);
TimeValue_2= xlabel('');
legend({'AccX', 'AccY', 'AccZ'},'Location','northwest','AutoUpdate','off');
axis([0 DATA(10,Nsamples) -20 20])

subplot(1,3,2)
plot(DATA(10,:),DATA(4,:),'r',DATA(10,:),DATA(5,:),'g',DATA(10,:),DATA(6,:),'b');
refline([0 0])
title('Angular velocity (rad/s)')
Timeline_3 = line('XData',x,'YData',y);
TimeValue_3= xlabel('');
legend({'GyroX', 'GyroY', 'GyroZ'},'Location','northwest','AutoUpdate','off');
axis([0 DATA(10,Nsamples) -3 3])

subplot(1,3,3)
plot(DATA(10,:),DATA(7,:),'r',DATA(10,:),DATA(8,:),'g',DATA(10,:),DATA(9,:),'b');
refline([0 0])
title('Magnetic flux density (uT)')
Timeline_4 = line('XData',x,'YData',y);
TimeValue_4= xlabel('');
legend({'MagX', 'MagY', 'MagZ'},'Location','northwest','AutoUpdate','off');
axis([0 DATA(10,Nsamples) -40 40])
