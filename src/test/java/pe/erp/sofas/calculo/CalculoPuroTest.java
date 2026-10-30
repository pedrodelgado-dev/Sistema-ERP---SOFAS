/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: CalculoPuroTest
 * Descripción: Reglas de calculo sin base de datos.
 *
 * @author PLMD - Pedro Luis Mamani Delgado
 * @author MGML - Maribel Gloria Mamani Lopez
 * @author AMCS - Alessandro Mijail Calcina Sanchez
 * @author DRJB - Danny Rodrigo Jove Benites
 * @version 1.0
 * @since 2026
 *
 * Identificación de autoría: equipo PLMD, MGML, AMCS y DRJB.
 * Conservar esta atribución en copias y modificaciones del código fuente.
 * =======================================================================
 */
package pe.erp.sofas.calculo;

import org.junit.jupiter.api.Test;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.calculo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Reglas de calculo sin base de datos. */
class CalculoPuroTest {

    private static Material mat(String codigo, String unidad, double costo) {
        return Material.builder().id((long) codigo.hashCode()).codigo(codigo).nombre(codigo).categoria("Insumos").unidad(unidad)
                .costo(costo).stock(100d).reservado(0d).minimo(0d).build();
    }

    private static FichaTecnica ficha(int l, int f, int a, int n) {
        return FichaTecnica.builder().codigo("FT-T").version(1).uid("FT-Tv1").estado(EstadoFicha.APROBADA).nombre("Prueba")
                .largo(l).fondo(f).alto(a).cuerpos(n).indirectosPct(6d).build();
    }

    private static Catalogo catalogo(Map<String, Material> mats) {
        return new Catalogo() {
            @Override
            public FichaTecnica fichaAprobada(String codigo) {
                return null;
            }

            @Override
            public Material material(String codigo) {
                return mats.get(codigo);
            }
        };
    }

    @Test
    void escaladoYMerma() {
        Material madera = mat("MP-001", "pt", 4.2);
        Material tela = mat("MP-003", "m", 28);
        Material patas = mat("MP-021", "unidad", 9);
        FichaTecnica f = ficha(210, 92, 85, 3);
        f.agregarLinea(LineaBOM.builder().material(madera).componente(Componente.ESTRUCTURA).cantidad(38d).mermaPct(18d).dependencia(Dependencia.L).build());
        f.agregarLinea(LineaBOM.builder().material(tela).componente(Componente.TAPIZADO).cantidad(9.5).mermaPct(10d).dependencia(Dependencia.T).build());
        f.agregarLinea(LineaBOM.builder().material(patas).componente(Componente.PATAS).cantidad(6d).mermaPct(0d).dependencia(Dependencia.P).build());
        f.agregarOperacion(OperacionMO.builder().etapa(EtapaMO.TAPIZADO).horas(10d).costoHora(9d).build());

        // Base: neta = base × 1 × cantidad; bruta = neta × (1 + merma)
        Explosion.Resultado base = Explosion.explotar(f, 2, Opciones.NINGUNA, catalogo(Map.of()));
        assertThat(base.lineas().get(0).neta()).isCloseTo(76, within(1e-9));
        assertThat(base.lineas().get(0).bruta()).isCloseTo(76 * 1.18, within(1e-9));
        assertThat(base.lineas().get(2).bruta()).isEqualTo(12);

        // A medida 160 × 92 × 85: L = 160/210, T = 160·177 / (210·177), patas 4/6
        Explosion.Resultado chico = Explosion.explotar(f, 1, Opciones.medidas(new Medidas(160, 92, 85, 2)), catalogo(Map.of()));
        assertThat(chico.lineas().get(0).factor()).isCloseTo(160.0 / 210, within(1e-9));
        assertThat(chico.lineas().get(1).bruta()).isCloseTo(9.5 * 160 / 210 * 1.10, within(1e-9));
        assertThat(chico.lineas().get(2).bruta()).isEqualTo(4);
        // Mano de obra escalada con (L/L0)^0.7
        assertThat(chico.costoMO()).isCloseTo(90 * Math.pow(160.0 / 210, 0.7), within(1e-9));
        // Costo estandar = materiales + MO + indirectos 6 %
        assertThat(base.costoEstandar()).isCloseTo((base.costoMateriales() + base.costoMO()) * 1.06, within(1e-6));

        // Las unidades enteras se redondean hacia arriba
        FichaTecnica g = ficha(210, 92, 85, 3);
        g.agregarLinea(LineaBOM.builder().material(mat("MP-014", "unidad", 1.8)).componente(Componente.COJINES).cantidad(6d)
                .mermaPct(5d).dependencia(Dependencia.N).build());
        assertThat(Explosion.explotar(g, 1, Opciones.medidas(new Medidas(160, 92, 85, 2)), catalogo(Map.of())).lineas().get(0).bruta())
                .isEqualTo(5); // 6 × 2/3 × 1.05 = 4.2 → 5
    }

    @Test
    void factoresDeLasFormulas() {
        Medidas base = new Medidas(210, 92, 85, 3);
        Medidas m = new Medidas(250, 100, 90, 4);
        assertThat(Formula.factor(Dependencia.F, m, base)).isEqualTo(1);
        assertThat(Formula.factor(Dependencia.LF, m, base)).isCloseTo(250.0 * 100 / (210 * 92), within(1e-9));
        assertThat(Formula.factor(Dependencia.LA, m, base)).isCloseTo(250.0 * 90 / (210 * 85), within(1e-9));
        assertThat(Formula.factor(Dependencia.T, m, base)).isCloseTo(250.0 * 190 / (210 * 177), within(1e-9));
        assertThat(Formula.factor(Dependencia.N, m, base)).isCloseTo(4.0 / 3, within(1e-9));
        assertThat(Formula.factor(Dependencia.P, new Medidas(150, 90, 85, 2), base)).isCloseTo(4.0 / 6, within(1e-9));
    }

    @Test
    void sustitucionDeTela() {
        Material chenille = mat("MP-003", "m", 28);
        Material terciopelo = mat("MP-007", "m", 42);
        FichaTecnica f = ficha(210, 92, 85, 3);
        f.agregarLinea(LineaBOM.builder().material(chenille).componente(Componente.TAPIZADO).cantidad(10d).mermaPct(10d).dependencia(Dependencia.T).build());
        Explosion.Resultado r = Explosion.explotar(f, 1, Opciones.sustituir(Map.of("MP-003", "MP-007")), catalogo(Map.of("MP-007", terciopelo)));
        assertThat(r.lineas().get(0).material().getCodigo()).isEqualTo("MP-007");
        assertThat(r.costoMateriales()).isCloseTo(11 * 42, within(1e-9));
        // Destino "-" quita la linea (zocalo en lugar de patas)
        Explosion.Resultado sin = Explosion.explotar(f, 1, Opciones.sustituir(Map.of("MP-003", Explosion.QUITAR)), catalogo(Map.of()));
        assertThat(sin.lineas()).isEmpty();
    }

    @Test
    void redondeoDeCompra() {
        assertThat(UnidadCompra.redondear("pt", 12)).isEqualTo(50);
        assertThat(UnidadCompra.redondear("pt", 51)).isEqualTo(100);
        assertThat(UnidadCompra.redondear("plancha", 2.3)).isEqualTo(3);
        assertThat(UnidadCompra.redondear("m", 23.5)).isEqualTo(30);
        assertThat(UnidadCompra.redondear("unidad", 3)).isEqualTo(10);
        assertThat(UnidadCompra.redondear("caja", 1.2)).isEqualTo(2);
        assertThat(UnidadCompra.redondear("galón", 0.4)).isEqualTo(1);
        assertThat(UnidadCompra.redondear("kg", 6)).isEqualTo(10);
        assertThat(UnidadCompra.redondear("kg", 0)).isEqualTo(0);
    }

    @Test
    void empaquetadoDeEspumaConAprovechamientoMinimo() {
        // Respaldos D18 4" de 2 sofas de 3 cuerpos: 6 piezas de 59.3 × 45 en planchas de 200 × 100.
        var piezas = Despiece.piezas(new Despiece.Sofa(210, 92, 85, 3, false, false, true, "OP-T"));
        List<Pieza> respaldo = piezas.get(Despiece.D18_4).stream().map(p -> p.conCantidad(p.cantidad() * 2)).toList();
        Corte.Resultado r = Corte.optimizar(TipoCorte.ESPUMA, respaldo, Corte.Estrategia.MEJOR_LADO);
        assertThat(r.colocaciones()).hasSize(6);
        assertThat(r.laminas()).isEqualTo(1);
        assertThat(r.aprovechamiento()).isGreaterThanOrEqualTo(75);
        // Ninguna pieza se sale de la plancha
        assertThat(r.colocaciones()).allMatch(c -> c.x() + c.w() <= 200.001 && c.y() + c.h() <= 100.001);
        // Las dos estrategias de "Recalcular" producen planos validos
        Corte.Resultado otra = Corte.optimizar(TipoCorte.ESPUMA, respaldo, Corte.Estrategia.MEJOR_AREA);
        assertThat(otra.colocaciones()).hasSize(6);
    }

    @Test
    void telaDeTerciopeloSinRotacion() {
        var piezas = Despiece.piezas(new Despiece.Sofa(210, 92, 85, 3, false, false, TipoTela.TERCIOPELO.permiteGiro(), "OP-T"));
        List<Pieza> tela = piezas.get(Despiece.TELA);
        assertThat(tela).allMatch(p -> !p.girable());
        assertThat(tela).anyMatch(p -> p.nombre().contains("(empalme)"));
        assertThat(tela).allMatch(p -> p.ancho() <= 140);
        Corte.Resultado r = Corte.optimizar(TipoCorte.TELA, tela, Corte.Estrategia.MEJOR_LADO);
        assertThat(r.colocaciones()).noneMatch(Corte.Colocacion::girada);
        assertThat(r.colocaciones()).allMatch(c -> c.x() + c.w() <= 140.001);
        // Con chenille se puede girar y el rollo nunca usa mas metros que sin giro
        var conGiro = Despiece.piezas(new Despiece.Sofa(210, 92, 85, 3, false, false, true, "OP-T")).get(Despiece.TELA);
        assertThat(Corte.optimizar(TipoCorte.TELA, conGiro, Corte.Estrategia.MEJOR_LADO).metros()).isLessThanOrEqualTo(r.metros() + 0.01);
    }

    @Test
    void maderaConKerf() {
        List<Pieza> p = List.of(new Pieza("Larguero", Componente.ESTRUCTURA, 150, 0, 2, false, "OP-T"));
        Corte.Resultado1D r = Corte.optimizar1D(p, 300);
        // 150 + 0.5 de kerf + 150 = 300.5 > 300 → dos tablas
        assertThat(r.tablas()).hasSize(2);
        List<Pieza> q = List.of(new Pieza("Travesaño", Componente.ESTRUCTURA, 70, 0, 4, false, "OP-T"));
        Corte.Resultado1D r2 = Corte.optimizar1D(q, 300);
        assertThat(r2.tablas()).hasSize(1);
        assertThat(r2.tablas().get(0).sobrante()).isCloseTo(300 - 280 - 1.5, within(1e-9));
    }

    @Test
    void coberturaDeGarantia() {
        LocalDate compra = LocalDate.of(2026, 3, 10);
        Cobertura.Resultado pata = Cobertura.evaluar("Pata floja", compra, LocalDate.of(2027, 3, 1));
        assertThat(pata.meses()).isEqualTo(12);
        assertThat(pata.cubierto()).isTrue();
        Cobertura.Resultado costura = Cobertura.evaluar("Costura abierta", compra, LocalDate.of(2026, 10, 1));
        assertThat(costura.meses()).isEqualTo(6);
        assertThat(costura.cubierto()).isFalse();
        assertThat(costura.vence()).isEqualTo(LocalDate.of(2026, 9, 10));
        assertThat(Cobertura.evaluar("Mecanismo trabado", LocalDate.of(2025, 11, 2), LocalDate.of(2026, 10, 2)).cubierto()).isTrue();
        assertThat(Cobertura.meses("Estructura quebrada")).isEqualTo(12);
        assertThat(Cobertura.meses("Espuma hundida")).isEqualTo(6);
    }

    @Test
    void igvIncluidoEnElPrecio() {
        assertThat(Igv.subtotal(new BigDecimal("2490.00"))).isEqualByComparingTo("2110.17");
        assertThat(Igv.igv(new BigDecimal("2490.00"))).isEqualByComparingTo("379.83");
        assertThat(Igv.subtotal(new BigDecimal("118.00"))).isEqualByComparingTo("100.00");
        assertThat(Igv.subtotal(new BigDecimal("118.00")).add(Igv.igv(new BigDecimal("118.00")))).isEqualByComparingTo("118.00");
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
