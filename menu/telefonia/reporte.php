<?php 
require_once $_SERVER['DOCUMENT_ROOT'] . '/menu/template_menu.php';
?>

<!-- ////////////////Info ventana///////////////// -->


<!-- Datepicker librerias -->
<script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
<link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" integrity="sha384-wvfXpqpZZVQGK6TAh5PVlGOfQNHSoD2xbE+QkPxCAFlNEevoEH3Sl0sibVcOQVnN" crossorigin="anonymous">
    <script src="https://unpkg.com/gijgo@1.9.14/js/gijgo.min.js" type="text/javascript"></script>
    <link href="https://unpkg.com/gijgo@1.9.14/css/gijgo.min.css" rel="stylesheet" type="text/css" />
<!-- Datepicker librerias -->


<main class="container">
<div class="bg-body-tertiary p-5 rounded">

<h2 class="text-center">Reporte de consumo Central Virtual</h2>
<br>


<?php

            $fecha_inicio="2024-02-27";
            $fecha_fin="2024-02-27";
            if ($_SERVER["REQUEST_METHOD"] == "POST") {
                if (isset($_POST['fecha_inicio']) and isset($_POST['fecha_fin'])){
                    $fecha_inicio_temp = htmlspecialchars($_POST['fecha_inicio']);
                    $fecha_fin_temp = htmlspecialchars($_POST['fecha_fin']);
                    if (!empty($fecha_inicio_temp) and !empty($fecha_fin_temp)) {
                      $fecha_inicio=$fecha_inicio_temp;
                      $fecha_fin=$fecha_fin_temp;
                    }
                }
              }

?>

<form action="<?php echo $_SERVER['PHP_SELF'];?>" method="post">
<div class="row">
    <div class="col-sm-3 d-flex align-items-center"> <h4>Filtrar reporte</h4></div>

    

        <div class="col-sm-3"> Fecha de inicio: <input id="startDate" name="fecha_inicio" value="<?php echo $fecha_inicio;?>"/></div>
            
        <div class="col-sm-3"> Fecha de fin: <input id="endDate" name="fecha_fin" value="<?php echo $fecha_fin;?>"/></div>
        <div class="col-sm-2"><button type="submit" class="btn btn-primary">Actualizar Reporte</button></div>

   
</div>
</form>

    <script>
        var today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());
        $('#startDate').datepicker({
            uiLibrary: 'bootstrap4',
            iconsLibrary: 'fontawesome',
            //format: 'dd-mm-yyyy',
            format: 'yyyy-mm-dd',
            //locale: 'es-es',

            //minDate: today,
            maxDate: function () {
                return $('#endDate').val();
            }
        });
        $('#endDate').datepicker({
            uiLibrary: 'bootstrap4',
            iconsLibrary: 'fontawesome',
            format: 'yyyy-mm-dd',
            //format: 'dd-mm-yyyy',
            maxDate: today,
            //locale: 'es-es',

            minDate: function () {
                return $('#startDate').val();
            }
        });
    </script>

<br>
<br>

    <!-- <h2>Reporte de consumo CV</h2> -->
    <table class="table table-hover">
        <thead>
            <tr>
                <th>Fecha</th>
                <th>Hora</th>
                <!-- Add more columns as needed -->
                <th>Tipo de llamada</th>
                <th>Numero que llama</th>
                <th>Numero llamado</th>
                <th>Conectada</th>
                <th>Duración</th>
            </tr>
        </thead>
        <tbody>

            <?php
       /*     $fecha_inicio="2024-01-01";
            $fecha_fin="2024-01-13";
            if ($_SERVER["REQUEST_METHOD"] == "POST") {
                if (isset($_POST['fecha_inicio']) and isset($_POST['fecha_fin'])){
                    $fecha_inicio_temp = htmlspecialchars($_POST['fecha_inicio']);
                    $fecha_fin_temp = htmlspecialchars($_POST['fecha_fin']);
                    if (!empty($fecha_inicio_temp) and !empty($fecha_fin_temp)) {
                      $fecha_inicio=$fecha_inicio_temp;
                      $fecha_fin=$fecha_fin_temp;
                    }
                }
              }
*/
            

            $api_url_session = 'https://commportal.winempresas.pe/login?version=9.5.40&DirectoryNumber=10000532&UserType=bgAdmin&Password=Opt*2023%23#';
            // Fetch data from the API
            $api_data_session = file_get_contents($api_url_session);
           // Usando explode para separar el string en un array
            $partes = explode("=", $api_data_session);
            // $partes ahora es un array con dos elementos: $partes[0] contiene "clave" y $partes[1] contiene "valor"
            $valor_session = $partes[1];



           
            error_reporting(E_ERROR | E_PARSE);
            $api_url = 'https://commportal.winempresas.pe/session'.$valor_session.'/bg/calllogs.csv?departmentFilter=*&initialDate='.$fecha_inicio.'T00:00:00Z&endDate='.$fecha_fin.'T23:59:59Z&downloadTo=nue';
           
          
            // Fetch data from the API
            $api_data = file_get_contents($api_url);


            // Convert CSV data to an array
            $csv_data = array_map('str_getcsv', explode("\n", $api_data));

            // Get headers from the first row
            $headers = array_shift($csv_data);

            // Find the indices of the desired columns
            $fecha_index = array_search('Fecha', $headers);
            $hora_index = array_search('Hora', $headers);
            // Add more columns as needed
            $tipo_llamada_index = array_search('Tipo de llamada', $headers);
            $numero_llama_index = array_search('Número que llama', $headers);
            $numero_llamado_index = array_search('Número llamado', $headers);
            $llamada_conectada_index = array_search('Llamada conectada', $headers);
            $duracion_index = array_search('Duración', $headers);



            // Exclude the last row from CSV data
            array_pop($csv_data);            
            // Iterate through the data and create table rows
            foreach ($csv_data as $row) {
                echo '<tr>';
                echo '<td>' . $row[$fecha_index] . '</td>';
                echo '<td>' . $row[$hora_index] . '</td>';
                // Add more columns as needed

                echo '<td>' . $row[$tipo_llamada_index] . '</td>';
                echo '<td>' . $row[$numero_llama_index] . '</td>';
                echo '<td>' . $row[$numero_llamado_index] . '</td>';
                echo '<td>' . $row[$llamada_conectada_index] . '</td>';
                echo '<td>' . $row[$duracion_index] . '</td>';
                echo '</tr>';
            }


             


 

            ?>

        </tbody>
    </table>






</div>
</main>

<!-- ///////////////////////////////////// -->
<script src="http://<?php echo $_SERVER['SERVER_NAME'];?>/assets/dist/js/bootstrap.bundle.min.js"></script>


    </body>
</html>