/**
 * =======================================================================
 *              MARCA DE AUTORIA DEL CODIGO FUENTE - 2026
 *              PLMD | MGML | AMCS | DRJB
 * =======================================================================
 * Sistema ERP para la Gestión de Fabricación y Comercialización de Sofás
 * Universidad Nacional de Juliaca
 * Escuela Profesional de Ingeniería de Software y Sistemas
 *
 * Clase: ConfigSofa
 * Descripción: Configuracion de un sofa en el configurador 3D (y para dibujarlo con SofaBuilder).
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
package pe.erp.sofas.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Configuracion de un sofa en el configurador 3D (y para dibujarlo con SofaBuilder). */
public record ConfigSofa(Modelo modelo, int cuerpos, Chaise chaise, int profundidad, Long telaId, Brazo brazo,
                         Patas patas, AcabadoMadera acabado, Espuma espuma, int cojines, Long telaCojinesId,
                         Mecanismo mecanismo) {

    public enum Modelo {
        MODERNO("Sofá Moderno", "SF-001", Forma.MODERNO),
        SECCIONAL_L("Seccional L", "SF-005", Forma.SECCIONAL_L),
        SILLON("Sillón", "SF-003", Forma.SILLON),
        CHESTERFIELD("Chesterfield", "SF-006", Forma.CHESTERFIELD),
        SOFA_CAMA("Sofá cama", "SF-007", Forma.SOFA_CAMA),
        ESCANDINAVO("Escandinavo", "SF-009", Forma.ESCANDINAVO);

        private final String nombre;
        private final String producto;
        private final Forma forma;

        Modelo(String nombre, String producto, Forma forma) {
            this.nombre = nombre;
            this.producto = producto;
            this.forma = forma;
        }

        public String nombre() {
            return nombre;
        }

        /** Codigo del modelo del catalogo del que toma el precio "desde". */
        public String producto() {
            return producto;
        }

        public Forma forma() {
            return forma;
        }
    }

    public enum Chaise {
        NINGUNA("Sin chaise"), IZQUIERDA("Izquierda"), DERECHA("Derecha");

        private final String nombre;

        Chaise(String nombre) {
            this.nombre = nombre;
        }

        public String nombre() {
            return nombre;
        }
    }

    public enum Brazo {
        RECTO("Recto"), REDONDEADO("Redondeado"), DELGADO("Delgado");

        private final String nombre;

        Brazo(String nombre) {
            this.nombre = nombre;
        }

        public String nombre() {
            return nombre;
        }
    }

    public enum Patas {
        MADERA("Madera cónica", null, 0, false),
        METAL_NEGRO("Metal negro", "MP-022", 14, false),
        METAL_DORADO("Metal dorado", "MP-023", 20, false),
        ZOCALO("Zócalo oculto", "MP-035", 0, true);

        private final String nombre;
        private final String material;
        private final double recargo;
        private final boolean bajoPedido;

        Patas(String nombre, String material, double recargo, boolean bajoPedido) {
            this.nombre = nombre;
            this.material = material;
            this.recargo = recargo;
            this.bajoPedido = bajoPedido;
        }

        public String nombre() {
            return nombre;
        }

        public String material() {
            return material;
        }

        /** Diferencia de precio por pata (+S/ 14 c/u, +S/ 20 c/u). */
        public double recargo() {
            return recargo;
        }

        public boolean bajoPedido() {
            return bajoPedido;
        }
    }

    public enum AcabadoMadera {
        NATURAL("Natural", "#C69A6D"), NOGAL("Nogal", "#7A4E2B"), WENGUE("Wengué", "#3B2A20");

        private final String nombre;
        private final String hex;

        AcabadoMadera(String nombre, String hex) {
            this.nombre = nombre;
            this.hex = hex;
        }

        public String nombre() {
            return nombre;
        }

        public String hex() {
            return hex;
        }
    }

    public enum Espuma {
        D23("Espuma D23 estándar", null, false),
        D30("D30 alta densidad", "MP-020", false),
        VISCO("Viscoelástica", "MP-034", true);

        private final String nombre;
        private final String material;
        private final boolean bajoPedido;

        Espuma(String nombre, String material, boolean bajoPedido) {
            this.nombre = nombre;
            this.material = material;
            this.bajoPedido = bajoPedido;
        }

        public String nombre() {
            return nombre;
        }

        public String material() {
            return material;
        }

        public boolean bajoPedido() {
            return bajoPedido;
        }
    }

    public enum Mecanismo {
        FIJO("Fijo", false),
        RECLINABLE_MANUAL("Reclinable manual", false),
        RECLINABLE_ELECTRICO("Reclinable eléctrico", true),
        CAMA("Cama plegable", false);

        private final String nombre;
        private final boolean bajoPedido;

        Mecanismo(String nombre, boolean bajoPedido) {
            this.nombre = nombre;
            this.bajoPedido = bajoPedido;
        }

        public String nombre() {
            return nombre;
        }

        public boolean bajoPedido() {
            return bajoPedido;
        }
    }

    /** Largo segun los cuerpos: 95 / 160 / 210 / 260 cm. */
    public static int largoPara(int cuerpos) {
        return switch (cuerpos) {
            case 1 -> 95;
            case 2 -> 160;
            case 3 -> 210;
            default -> 260;
        };
    }

    public int largo() {
        return largoPara(cuerpos);
    }

    public int alto() {
        return modelo == Modelo.CHESTERFIELD ? 78 : 85;
    }

    public boolean conChaise() {
        return chaise != null && chaise != Chaise.NINGUNA;
    }

    public ConfigSofa con(Modelo m) {
        return new ConfigSofa(m, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conCuerpos(int n) {
        return new ConfigSofa(modelo, n, chaise, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conChaise(Chaise c) {
        return new ConfigSofa(modelo, cuerpos, c, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conProfundidad(int p) {
        return new ConfigSofa(modelo, cuerpos, chaise, p, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conTela(Long t) {
        return new ConfigSofa(modelo, cuerpos, chaise, profundidad, t, brazo, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conBrazo(Brazo b) {
        return new ConfigSofa(modelo, cuerpos, chaise, profundidad, telaId, b, patas, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conPatas(Patas p) {
        return new ConfigSofa(modelo, cuerpos, chaise, profundidad, telaId, brazo, p, acabado, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conAcabado(AcabadoMadera a) {
        return new ConfigSofa(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, a, espuma, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conEspuma(Espuma e) {
        return new ConfigSofa(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, e, cojines, telaCojinesId, mecanismo);
    }

    public ConfigSofa conCojines(int n, Long tela) {
        return new ConfigSofa(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, espuma, n, tela, mecanismo);
    }

    public ConfigSofa conMecanismo(Mecanismo m) {
        return new ConfigSofa(modelo, cuerpos, chaise, profundidad, telaId, brazo, patas, acabado, espuma, cojines, telaCojinesId, m);
    }

    public String serializar() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("modelo", modelo.name());
        m.put("cuerpos", String.valueOf(cuerpos));
        m.put("chaise", chaise.name());
        m.put("prof", String.valueOf(profundidad));
        m.put("tela", telaId == null ? "" : telaId.toString());
        m.put("brazo", brazo.name());
        m.put("patas", patas.name());
        m.put("acabado", acabado.name());
        m.put("espuma", espuma.name());
        m.put("cojines", String.valueOf(cojines));
        m.put("telaCojines", telaCojinesId == null ? "" : telaCojinesId.toString());
        m.put("mec", mecanismo.name());
        StringBuilder sb = new StringBuilder();
        m.forEach((k, v) -> sb.append(k).append('=').append(v).append(';'));
        return sb.toString();
    }

    public static ConfigSofa leer(String s) {
        if (s == null || s.isBlank()) return null;
        Map<String, String> m = new LinkedHashMap<>();
        for (String par : s.split(";")) {
            int i = par.indexOf('=');
            if (i > 0) m.put(par.substring(0, i), par.substring(i + 1));
        }
        try {
            return new ConfigSofa(Modelo.valueOf(m.get("modelo")), Integer.parseInt(m.get("cuerpos")),
                    Chaise.valueOf(m.get("chaise")), Integer.parseInt(m.get("prof")), id(m.get("tela")),
                    Brazo.valueOf(m.get("brazo")), Patas.valueOf(m.get("patas")), AcabadoMadera.valueOf(m.get("acabado")),
                    Espuma.valueOf(m.get("espuma")), Integer.parseInt(m.get("cojines")), id(m.get("telaCojines")),
                    Mecanismo.valueOf(m.get("mec")));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Long id(String s) {
        return s == null || s.isBlank() ? null : Long.valueOf(s);
    }
}

/* MARCA DE AUTORIA: PLMD | MGML | AMCS | DRJB - UNJ 2026 */
