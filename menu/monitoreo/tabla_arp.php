<?php 
require_once $_SERVER['DOCUMENT_ROOT'] . '/menu/template_menu.php';
?>

<!-- ////////////////Info ventana///////////////// -->
<main class="container">
<div class="bg-body-tertiary p-5 rounded">



<?php
$arrContextOptions=array(
    "ssl"=>array(
        "verify_peer"=>false,
        "verify_peer_name"=>false,
    ),
);  
 
$json = file_get_contents('https://191.98.171.118/api/v2/monitor/network/arp?access_token=Nqtr71rgz98x11xmwwdd8kr4yqjf53', false, stream_context_create($arrContextOptions));
 
$data = json_decode($json, true);

?>



<div class="row">
    <div class="col-sm-5">
    <h2 style="margin-bottom:2rem;">Tabla ARP</h2>
</div>
<div class="col-sm-2">
<h4 style="margin-bottom:2rem;">Mis equipos: </h4>
</div>

<div class="col-sm-4" >
<select class="form-control" id="visibilitySelector" onchange="toggleTableVisibility()">
  <option value="visible">Fortinet - Fortigate 60F - 191.98.171.118</option>
  <option value="hidden">YYYYY - 191.98.XXX.XXX</option>
  <option value="hidden">ZZZZZ - 191.98.XXX.XXX</option>
</select>
</div>
</div>



  <table class="table table-hover" id="miTabla">
        <tr>
            <th>IP</th>
            <th>Age</th>
            <th>MAC</th>
            <th>Interface</th>
        </tr>

        <?php foreach ($data['results'] as $results): ?>
            <tr>
                <td><?= $results['ip'] ?></td>
                <td><?= $results['age'] ?></td>
                <td><?= $results['mac'] ?></td>
                <td><?= $results['interface'] ?></td>
            </tr>
        <?php endforeach; ?>

    </table>






</div>  
</main>


<!-- ///////////////////////////////////// -->
<script src="http://<?php echo $_SERVER['SERVER_NAME'];?>/assets/dist/js/bootstrap.bundle.min.js"></script>


<script>
    function toggleTableVisibility() {
      // Obtiene el valor seleccionado del combobox
      var visibility = document.getElementById("visibilitySelector").value;

      // Obtén la referencia a la tabla
      var table = document.getElementById("miTabla");

      // Cambia la propiedad de visibilidad de la tabla según la selección
      table.style.visibility = visibility;
    }
  </script>

    </body>
</html>