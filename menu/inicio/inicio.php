<?php 
require_once $_SERVER['DOCUMENT_ROOT'] . '/menu/template_menu.php';
?>

<!-- ////////////////Info ventana///////////////// -->
<main class="container">
<div class="bg-body-tertiary p-5 rounded">



    <h1>Bienvenido <B>{EMPRESA}</B>,</h1>
    <p class="lead">En este portal puede obtener información de sus equipos y servicios contratados con Win Empresas, actualmente tiene los siguientes CID activos:</p>

    <table class="table">
      <thead>
        <tr>
          <!-- Encabezados de la tabla -->
          <th>CID</th>
          <th>Servicio</th>
          <th>Inicio</th>
          <th>Fin</th>
        </tr>
      </thead>
      <tbody>
        <!-- Filas de la tabla con datos -->
        <tr>
          <td>6046464</td>
          <td>Internet dedicado</td>
          <td>01/05/2023</td>
          <td>01/05/2028</td>
        </tr>
        <tr>
          <td>6046465</td>
          <td>Central Virtual On Demand</td>
          <td>01/05/2023</td>
          <td>01/05/2028</td>
        </tr>
        <tr>
          <td>6046466</td>
          <td>Seguridad perimetral</td>
          <td>01/05/2023</td>
          <td>01/05/2028</td>
        </tr>
      </tbody>
    </table>
    
    <br>
    <p class="lead">Para cambios o consultas en sus servicios contacte su gerente de cuenta asignado: <b>{NOMBRE}</b></p>

    <div class="text-center">
    <a class="btn btn-lg btn-primary" href="" role="button">Contactar</a>
  
    </div>





</div>  
</main>


<!-- ///////////////////////////////////// -->
<script src="http://<?php echo $_SERVER['SERVER_NAME'];?>/assets/dist/js/bootstrap.bundle.min.js"></script>

    </body>
</html>