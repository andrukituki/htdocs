<?php
	$usuario = "";$clave="";
	date_default_timezone_set("America/Lima");

	if($_SERVER["REQUEST_METHOD"] == "POST")
	{   
		$usuario = $_POST["usuario"];
		$clave = $_POST["clave"];

			
		if($usuario=='admin' && $clave=="admin"){
			
            session_start();
			$_SESSION['usuario']=$usuario;
			$_SESSION['admin']=TRUE;
			$_SESSION['hora_ingreso']=date("Y-n-j H:i:s");

            //header("Location:../menu/menu.php");
			//header("Location:http://localhost/menu/monitoreo/tabla_arp.php");
			header("Location:http://".$_SERVER['SERVER_NAME']."/menu/inicio/inicio.php");
			return;
		}

		else{
			/*Clave o usuario errados*/
			echo "<script language=javascript>
			alert('Usuario o clave errados. Por favor, verifique sus datos.')
			self.location='../index.php'</script>";
		}
		
	}
    else{
        echo "<script language=javascript>
        alert('Ingrese los datos')
        self.location='../index.php'</script>";
    }



?>