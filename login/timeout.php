<?php
	/*date_default_timezone_get("America/Lima");*/

	$fecha_antigua=$_SESSION['hora_ingreso'];
	$hora = date("Y-n-j H:i:s");
	$tiempo = (strtotime($hora)-strtotime($fecha_antigua));
	$tiempo = date('s', $tiempo);
	if($tiempo>=600){
		session_unset();
		session_destroy();
		echo '<script language=javascript>
			alert("Su sesi\u00f3n ha terminado por seguridad al exceder los 10 minutos.")
			self.location="../index.php"</script>';
		
	}
	else{
		/*Actualizamos la actividad para darle 10 minutos más al usuario*/
		$_SESSION['hora_ingreso']=$hora;
	}

?>