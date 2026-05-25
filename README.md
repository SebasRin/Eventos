Integrante : Juan Sebastian Rincon Cañon

Descripción del Proyecto
Plataforma de gestión de eventos y venta de entradas desarrollada en Java con JavaFX. Permite a usuarios finales explorar eventos (conciertos, teatro, conferencias), seleccionar zonas y asientos, comprar entradas, agregar servicios adicionales (VIP, seguro de cancelación, merchandising, parqueadero), pagar y recibir notificaciones sobre cambios de estado.
La plataforma cuenta con dos perfiles:

Usuario: Explora eventos, compra entradas, gestiona su perfil y descarga reportes de sus compras.

Administrador: Gestiona eventos, recintos, zonas, asientos, usuarios, compras, incidencias y visualiza métricas.

Instrucciones para Compilar y Ejecutar

Compilar y Ejecutar (en el terminal)
Opción 1: Usando Maven Wrapper

# En Windows
cd C:\ruta\del\proyecto\Eventos.\mvnw.cmd javafx:run

# En Mac/Linux
cd /ruta/del/proyecto/Eventos./mvnw javafx:run

Patrones de Diseño Implementados
Patrones Creacionales
1. Singleton - GestorSistema
Aspecto	Descripción
RF	RF-013, RF-015, RF-025
Problema	El sistema necesita un único punto de control para el catálogo de eventos y disponibilidad de asientos. Múltiples instancias causarían inconsistencias.
Propósito	Centralizar la gestión del catálogo y control de disponibilidad con inicialización lazy y thread-safe.
Solución	Constructor privado y método estático con double-checked locking.
java
public class GestorSistema {
    private static volatile GestorSistema instancia;
    
    private GestorSistema() {
        repositorioEventos = new RepositorioEventos();
        repositorioUsuarios = new RepositorioUsuarios();
        // ...
    }
    
    public static GestorSistema getInstance() {
        if (instancia == null) {
            synchronized (GestorSistema.class) {
                if (instancia == null) {
                    instancia = new GestorSistema();
                }
            }
        }
        return instancia;
    }
}
2. Factory Method - EventoFactory
Aspecto	Descripción
RF	RF-023, RF-013
Problema	Los eventos pueden ser de varios tipos (Concierto, Teatro, Conferencia) con reglas distintas de aforo y políticas.
Propósito	Delegar la creación de eventos a subclases especializadas, cumpliendo OCP.
Solución	Clase abstracta EventoFactory con método crearEvento() implementado por fábricas concretas.
java
public abstract class EventoFactory {
    public abstract Evento crearEvento(String nombre, String descripcion,
                                       String ciudad, LocalDateTime fechaHora,
                                       Recinto recinto);
}

public class ConciertoFactory extends EventoFactory {
    @Override
    public Evento crearEvento(String nombre, String descripcion,
                              String ciudad, LocalDateTime fechaHora,
                              Recinto recinto) {
        return new Evento(nombre, CategoriaEvento.CONCIERTO, descripcion,
                ciudad, fechaHora, recinto);
    }
}
3. Builder - CompraBuilder
Aspecto	Descripción
RF	RF-034, RF-035, RF-009
Problema	Una compra se construye en pasos: usuario, evento, entradas, servicios adicionales. Constructor con muchos parámetros sería ilegible.
Propósito	Permitir construcción paso a paso de objetos Compra con validación final.
Solución	Clase Builder con métodos encadenados y método build() que valida antes de crear.
java
public class CompraBuilder {
    private Usuario usuario;
    private Evento evento;
    private final List<Entrada> entradas = new ArrayList<>();
    private final List<String> servicios = new ArrayList<>();
    
    public CompraBuilder setUsuario(Usuario u) { this.usuario = u; return this; }
    public CompraBuilder addEntrada(Entrada e) { entradas.add(e); return this; }
    
    public Compra build() {
        validar();
        return new Compra(usuario, evento, entradas, servicios);
    }
}
Patrones Estructurales
1. Decorator - IServicioAdicional
Aspecto	Descripción
RF	RF-009, RF-035
Problema	Los servicios adicionales (VIP, seguro, merchandising, parqueadero) se pueden combinar libremente sobre una compra base.
Propósito	Añadir responsabilidades en tiempo de ejecución sin herencia.
Solución	Decorador abstracto que envuelve componentes y decora con funcionalidad extra.
java
public interface IServicioAdicional {
    String getDescripcion();
    double getPrecioExtra();
}

public class ServicioVIPDecorator implements IServicioAdicional {
    private final IServicioAdicional wrapped;
    
    @Override
    public String getDescripcion() {
        return wrapped.getDescripcion() + " + Acceso VIP";
    }
    
    @Override
    public double getPrecioExtra() {
        return wrapped.getPrecioExtra() + 80_000;
    }
}
2. Adapter - PasarelaPagoAdapter
Aspecto	Descripción
RF	RF-007, RF-016, RF-046
Problema	El sistema debe interactuar con pasarelas de pago externas con interfaces incompatibles.
Propósito	Traducir una interfaz incompatible a la que espera el cliente.
Solución	Adaptador que implementa la interfaz esperada y delega al sistema externo.
java
public class PasarelaPagoAdapter implements IPasarelaPago {
    private final SistemaPagoExterno externo = new SistemaPagoExterno();
    
    @Override
    public boolean procesar(double monto, String metodoPago) {
        TransaccionDTO dto = new TransaccionDTO(monto, metodoPago);
        RespuestaDTO r = externo.ejecutarTransaccion(dto);
        return r.exitosa();
    }
}
3. Facade - PlataformaFacade
Aspecto	Descripción
RF	RF-003, RF-006, RF-007, RF-011, RF-046
Problema	La UI necesita coordinar múltiples subsistemas (GestorSistema, CompraBuilder, PasarelaPago, etc.).
Propósito	Proveer una interfaz simplificada sobre los subsistemas complejos.
Solución	Clase Facade que unifica operaciones comunes como comprar, cancelar y generar reportes.
java
public class PlataformaFacade {
    private static PlataformaFacade instancia;
    private final HistorialComandos historial = new HistorialComandos();
    
    public Compra crearCompra(Usuario usuario, Evento evento,
                              List<Entrada> entradas, List<String> servicios) {
        CompraBuilder builder = new CompraBuilder()
                .setUsuario(usuario)
                .setEvento(evento);
        for (Entrada e : entradas) builder.addEntrada(e);
        return builder.build();
    }
    
    public byte[] generarReporte(TipoReporte tipo, LocalDate desde, 
                                 LocalDate hasta, String formato) {
        // ...
    }
}
Patrones de Comportamiento
1. Strategy - IEstrategiaPago
Aspecto	Descripción
RF	RF-007, RF-021
Problema	El sistema soporta múltiples métodos de pago (tarjeta, PSE, efectivo) con lógica diferente.
Propósito	Encapsular algoritmos de pago intercambiables sin modificar el contexto.
Solución	Interfaz IEstrategiaPago con implementaciones concretas.
java
public interface IEstrategiaPago {
    boolean pagar(double monto);
    boolean reembolsar(double monto);
    String getDescripcion();
}

public class PagoTarjeta implements IEstrategiaPago {
    @Override
    public boolean pagar(double monto) {
        System.out.printf("[PAGO] Tarjeta %s *%s — $%.0f aprobado%n", tipo, numero, monto);
        return true;
    }
}
2. Observer - IObservadorCompra
Aspecto	Descripción
RF	RF-008, RF-017
Problema	Cuando una compra cambia de estado, se deben enviar notificaciones por email, SMS y registrar incidencias.
Propósito	Desacoplar el sujeto (Compra) de sus observadores (notificadores).
Solución	Interfaz IObservadorCompra y métodos agregarObservador()/cambiarEstado().
java
public class Compra {
    private final List<IObservadorCompra> observadores = new ArrayList<>();
    
    private void cambiarEstado(EstadoCompra nuevo) {
        EstadoCompra anterior = this.estado;
        this.estado = nuevo;
        for (IObservadorCompra o : observadores) {
            o.compraActualizada(this, anterior);
        }
    }
}

public class NotificadorEmail implements IObservadorCompra {
    @Override
    public void compraActualizada(Compra compra, EstadoCompra estadoAnterior) {
        // Enviar email al usuario
    }
}
3. Command - IComandoCompra
Aspecto	Descripción
RF	RF-006, RF-036, RF-016
Problema	Las operaciones sobre compras deben registrarse para auditoría y soportar deshacer.
Propósito	Encapsular solicitudes como objetos para historial y undo.
Solución	Interfaz IComandoCompra con métodos ejecutar() y deshacer().
java
public interface IComandoCompra {
    void ejecutar();
    void deshacer();
    String getDescripcion();
}

public class ComandoCancelarCompra implements IComandoCompra {
    private final Compra compra;
    
    @Override
    public void ejecutar() {
        compra.cancelar();
    }
    
    @Override
    public void deshacer() {
        System.out.println("[UNDO] Cancelación revertida");
    }
}
Principios SOLID Aplicados
1. Single Responsibility Principle (SRP)
Ejemplo: CompraBuilder solo se encarga de construir compras, no de persistirlas ni notificar.

java
// SRP: Solo construcción
public class CompraBuilder {
    public Compra build() { ... }
}

//  Antes: La misma clase hacía todo
// CompraBuilder guardaba, notificaba y construía
2. Open/Closed Principle (OCP)
Ejemplo: EventoFactory permite agregar nuevos tipos de evento sin modificar código existente.

java
//  Abierto para extensión
public class ConferenciaFactory extends EventoFactory { ... }
public class TeatroFactory extends EventoFactory { ... }

//  Cerrado para modificación - no necesitamos cambiar EventoFactory
3. Liskov Substitution Principle (LSP)
Ejemplo: Todas las estrategias de pago pueden reemplazar a IEstrategiaPago.

java
//  Cualquier estrategia puede sustituir a IEstrategiaPago
IEstrategiaPago pago = new PagoTarjeta("Visa", "1234");
pago.pagar(100000);  // Funciona
pago = new PagoPSE("Bancolombia");
pago.pagar(100000);  // También funciona
4. Interface Segregation Principle (ISP)
Ejemplo: Interfaces específicas en lugar de una interfaz grande.

java
//  Interfaces segregadas
public interface IEstrategiaPago {
    boolean pagar(double monto);
    boolean reembolsar(double monto);
}

public interface IObservadorCompra {
    void compraActualizada(Compra compra, EstadoCompra anterior);
}

//  No tenemos una interfaz "IPagoTodo" con métodos no relacionados
5. Dependency Inversion Principle (DIP)
Ejemplo: PlataformaFacade depende de abstracciones (IGeneradorReporte) no de implementaciones concretas.

java
//  Depende de abstracción
public byte[] generarReporte(..., String formato) {
    IGeneradorReporte generador;
    if ("PDF".equals(formato)) {
        generador = new GeneradorPDF();  // Inyección en tiempo de ejecución
    } else {
        generador = new GeneradorCSV();
    }
    return generador.generar(...);
}

//  No depende directamente de clases concretas en toda la lógica
