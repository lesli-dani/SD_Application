September 2022 - May 2023

This is the code for a Senior Design Application for a system created by Team MicRoGV.
The team consisted of two Computer Engineers and two Electrical Engineers.

[Final Report Link](https://drive.google.com/file/d/1oq_fJl-bd84QJuFUfe5cixuaa_xg4-pZ/view?usp=sharing)

Final Application:
<br>
Splash Screen:
<br>
<img width="172" alt="image" src="https://github.com/lesli-dani/SD_Application/assets/72846459/eff944f1-6bce-4132-99d0-1d5f8ce3593c">
<br>
Data Screen:
<br>
<img width="208" alt="image" src="https://github.com/lesli-dani/SD_Application/assets/72846459/beec1512-c2be-4176-a913-c881e465db38">
<br>
Figma:
<br>
<img width="478" alt="image" src="https://github.com/lesli-dani/SD_Application/assets/72846459/95ab4d83-8a3d-4d28-9134-f9b7f48a5dd3">

Electrical Team & half of Computer Engineering Team focused on:<br>
Renewable energy sources are commonly used in a variety of applications within the energy grid. 
In order to effectively harness energy from these sources, it is necessary to calculate the maximum power at any given time. 
This system implements the Maximum Power Point Tracking (MPPT) using the Perturb and Observe(P&O) method to modify the 
Pulse-width modulation (PWM) signal to increase the power of the Photovoltaic (PV) system. The system design consists of 
two Boost converters: one for the MPPT algorithm and the other for load regulation at the battery. The MPPT algorithm is 
applied to the PV system as the input. An AC/DC converter with a voltage regulator will charge the battery when necessary. 

Other Half of the Computer Engineering Team focused on:<br>
Additionally, a mobile application was developed to display various sensor readings, such as current, voltage, temperature, 
irradiance and shutoff time. The hardware performance will be shown to the user through the data being displayed.

Data shown from Arduino:<br>
Temperature in Celsius and Fahrenheit<br>
Battery Percentage<br>
Raw Solar Panel Data -> Current, Voltage, Power before MPPT<br>
Output after MPPT -> Current, Voltage, Power<br>
API irradiance -> https://api.solcast.com.au/world_radiation/estimated_actuals?latitude=25.893907&longitude=-97.48691&output_parameters=ghi&format=json&api_key=Azvpit04Ws4BuY5jI8_4l-3KcGPJ-BGi&timezone=America%2FChicago
<br>API sunrise/sunset data -> https://api.sunrise-sunset.org/json?lat=25.901747&lng=-97.497482&date=today

