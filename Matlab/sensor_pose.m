freq=100;
Ts=1/freq;
%%%%% USE: TYPE_GYROSCOPE
%AngularVelocity_deg=AngularVelocity.Variables*180/pi;
%RotMatrix=cumsum((AngularVelocity_deg(1:end-1,1:end)+AngularVelocity_deg(2:end,1:end))*Ts*0.5);
%%%%%%
ps = [0 0 0];   %start position
patch = poseplot(); %open 3d object plot
ylim([-2 2])    %limit axes
xlim([-2 2])
zlim([-2 2])
xlabel("x (m)") %set labels
ylabel("y (m)")
zlabel("z (m)");
set(gca,'ZDir','normal');   %set orientation of plot
set(gca,'YDir','normal');
set(gca,'XDir','normal');
sz=size(RotMatrix);
for coeff = 1:size(RotMatrix,1) %replace size for TYPE_ROTATION_VECTOR
    coeff
    %q = quaternion(RotMatrix(coeff,:),'rotvecd') ;  %USE: TYPE_GYROSCOPE
    q=quaternion(RotationVector(coeff,2:end)); %USE: TYPE_ROTATION_VECTOR
    set(patch,Orientation=q,Position=ps); 
    drawnow
end