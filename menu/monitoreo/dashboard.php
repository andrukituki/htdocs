<?php 
require_once $_SERVER['DOCUMENT_ROOT'] . '/menu/template_menu.php';
?>

<!-- ////////////////Info ventana///////////////// -->
<main class="container">
<div class="bg-body-tertiary p-5 rounded">
<!-- ///////////////////////////////////// -->




<div class="row">
    <div class="col-sm-5">
    <h2>Dashboard de monitoreo</h2>
</div>
<div class="col-sm-2">
<h4 >Mis equipos: </h4>
</div>

<div class="col-sm-4" >
<select class="form-control" id="visibilitySelector" onchange="toggleTableVisibility()">
  <option value="visible">Fortinet - Fortigate 60F - 191.98.171.118</option>
  <option value="hidden">YYYYY - 191.98.XXX.XXX</option>
  <option value="hidden">ZZZZZ - 191.98.XXX.XXX</option>
</select>
</div>
</div>


<br>



<div class="row">
<div class="col-sm-6" style="heigth:30rem;" >
<canvas class="my-4 chartjs-render-monitor" id="myChart" width="732" height="308" style="display: block; height: 1rem; width: 2rem;"></canvas>
</div>

<div class="col-sm-6" style="heigth:30rem;" >
<canvas class="my-4 chartjs-render-monitor" id="myChart1" width="732" height="308" style="display: block; height: 154px; width: 366px;"></canvas>
</div>
</div>

<div>
<canvas class="my-4 chartjs-render-monitor" id="myChart2" width="732" height="308" style="display: block; height: 154px; width: 366px;"></canvas>
</div>
<!-- ///////////////////////////////////// -->
</div>  
</main>
<!-- ///////////////////////////////////// -->
<script src="http://<?php echo $_SERVER['SERVER_NAME'];?>/assets/dist/js/bootstrap.bundle.min.js"></script>

<!-- ///////////////////////////////////// -->
    <!-- Graphs -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.7.1/dist/Chart.min.js"></script>
    <script>
      var ctx = document.getElementById("myChart");
      var myChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
          datasets: [{
            data: [15339, 21345, 18483, 24003, 23489, 24092, 12034],
            lineTension: 0,
            backgroundColor: 'transparent',
            borderColor: '#007bff',
            borderWidth: 4,
            pointBackgroundColor: '#007bff'
          }]
        },
        options: {
          scales: {
            yAxes: [{
              ticks: {
                beginAtZero: false
              }
            }]
          },
          legend: {
            display: true,
          }
        }
      });


      var ctx = document.getElementById("myChart1");
      var myChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
          datasets: [{
            data: [15339, 21345, 18483, 24003, 23489, 24092, 12034],
            lineTension: 0,
            backgroundColor: 'transparent',
            borderColor: '#007bff',
            borderWidth: 4,
            pointBackgroundColor: '#007bff'
          }]
        },
        options: {
          scales: {
            yAxes: [{
              ticks: {
                beginAtZero: false
              }
            }]
          },
          legend: {
            display: false,
          }
        }
      });







      const data = {
  labels: [
    'Red',
    'Blue',
    'Yellow'
  ],
  datasets: [{
    label: 'My First Dataset',
    data: [300, 50, 100],
    backgroundColor: [
      'rgb(255, 99, 132)',
      'rgb(54, 162, 235)',
      'rgb(255, 205, 86)'
    ],
    hoverOffset: 4
  }]
};

const config = {
  type: 'doughnut',
  data: data,
};

var ctx = document.getElementById("myChart2");
      var myChart = new Chart(ctx,config);
    //   myChart.width = 10;
    //   myChart.height = 10;
      
    </script>
<!-- ///////////////////////////////////// -->

    </body>
</html>

