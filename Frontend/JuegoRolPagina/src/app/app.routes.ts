import { Routes } from '@angular/router';
import { Landing } from './landing/landing';
import { OpcionesComponent } from './opciones.component';
import { CombateComponent } from './combate/combate.component';
import { SelectorComponent } from './selectorELIMINAR.component';
import { SelectorPersonajeComponent } from './selectorPersonaje/selector.component';;
import { UsuarioWebComponent } from './usuario/usuarioWeb';
import { SelectorMasterComponent} from './selector-master/selector-master.component'
import { EditarPersonaje } from './editar-personaje/editar-personaje';
import { LoginComponent } from './login/login.component';
import { LanzadorDadosComponent } from './lanzador-dados/lanzador-dados.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dado', component: LanzadorDadosComponent },
  { path: '', component: Landing }, //Landing page 
  { path: 'crear-partida', component: OpcionesComponent },//Crear partida 
  { path: 'selector/:id', component: SelectorComponent }, //seleccionar rol dentro de una partida 
  { path: 'selector-personaje/:idPartida/jugar-combate/:id', component: CombateComponent }, // Entrar a un combate creado
  { path: 'perfil', component: UsuarioWebComponent }, //Entrar al perfil (no parece haber boton para entrar aqui)
  { path: 'selector-master/:id', component: SelectorMasterComponent}, //Elegir el personaje a editar 
  { path: 'selector-master/:idPartida/editar-personaje/:id', component: EditarPersonaje}, //Interfaz para editar un personaje en concreto 
  { path: 'selector-personaje/:id', component: SelectorPersonajeComponent }, //Seleccionar los personajes que van a combatir
];
