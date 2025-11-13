package com.apsbiometria.aps_biometria.authentication;

import com.apsbiometria.aps_biometria.model.AccessLevel;

public class AccessController {

    private final SessionManager sessionManager;

    public AccessController() {
        this.sessionManager = SessionManager.getInstance();
    }

    public static class AccessResult {
        private boolean granted;
        private String message;
        private AccessLevel userLevel;
        private AccessLevel requiredLevel;

        public AccessResult(boolean granted, String message, AccessLevel userLevel, AccessLevel requiredLevel) {
            this.granted = granted;
            this.message = message;
            this.userLevel = userLevel;
            this.requiredLevel = requiredLevel;
        }

        public boolean isGranted() {
            return granted;
        }

        public String getMessage() {
            return message;
        }

        public AccessLevel getUserLevel() {
            return userLevel;
        }

        public AccessLevel getRequiredLevel() {
            return requiredLevel;
        }

        @Override
        public String toString() {
            return String.format("Access %s: %s (User: %s, Required: %s)",
                    granted ? "GRANTED" : "DENIED",
                    message,
                    userLevel != null ? userLevel.toDisplayString() : "N/A",
                    requiredLevel != null ? requiredLevel.toDisplayString() : "N/A");
        }
    }

    public AccessResult checkAccess(String sessionId, AccessLevel requiredLevel) {

        Session session = sessionManager.getSession(sessionId);

        if (session == null) {
            return new AccessResult(false, "Sessão inválida ou expirada", null, requiredLevel);
        }

        if (!session.isActive()) {
            return new AccessResult(false, "Sessão expirada",
                    session.getUser().getAccessLevel(), requiredLevel);
        }

        if (session.hasAccess(requiredLevel)) {
            return new AccessResult(true, "Acesso concedido",
                    session.getUser().getAccessLevel(), requiredLevel);
        }

        return new AccessResult(false,
                String.format("Nível de acesso insuficiente. Necessário: %s, Atual: %s",
                        requiredLevel.toDisplayString(),
                        session.getUser().getAccessLevel().toDisplayString()),
                session.getUser().getAccessLevel(),
                requiredLevel);
    }

    public AccessResult checkPublicAccess(String sessionId) {
        return checkAccess(sessionId, AccessLevel.NIVEL_1);
    }

    public AccessResult checkDirectorAccess(String sessionId) {
        return checkAccess(sessionId, AccessLevel.NIVEL_2);
    }

    public AccessResult checkMinisterAccess(String sessionId) {
        return checkAccess(sessionId, AccessLevel.NIVEL_3);
    }

    public String getPublicData(String sessionId) {
        AccessResult access = checkPublicAccess(sessionId);

        if (!access.isGranted()) {
            return "ACESSO NEGADO: " + access.getMessage();
        }

        return generatePublicReport();
    }

    public String getDirectorData(String sessionId) {
        AccessResult access = checkDirectorAccess(sessionId);

        if (!access.isGranted()) {
            return "ACESSO NEGADO: " + access.getMessage();
        }

        return generateDirectorReport();
    }

    public String getMinisterData(String sessionId) {
        AccessResult access = checkMinisterAccess(sessionId);

        if (!access.isGranted()) {
            return "ACESSO NEGADO: " + access.getMessage();
        }

        return generateMinisterReport();
    }

    private String generatePublicReport() {
        StringBuilder report = new StringBuilder();
        report.append("======================================\n");
        report.append("   INFORMAÇÕES PÚBLICAS - NÍVEL 1\n");
        report.append("======================================\n\n");

        report.append("Propriedades Rurais Cadastradas:\n");
        report.append("  • Total de propriedades: 15.234\n");
        report.append("  • Área total monitorada: 2.450.000 hectares\n");
        report.append("  • Propriedades regularizadas: 12.890 (84.6%)\n\n");

        report.append("Estatísticas Gerais:\n");
        report.append("  • Regiões monitoradas: 5\n");
        report.append("  • Municípios abrangidos: 342\n");
        report.append("  • Última atualização: Hoje\n\n");

        report.append("Informações Disponíveis:\n");
        report.append("  ✓ Localização de propriedades\n");
        report.append("  ✓ Área total das propriedades\n");
        report.append("  ✓ Status de regularização\n");
        report.append("  ✓ Culturas principais\n\n");

        report.append("======================================\n");

        return report.toString();
    }

    private String generateDirectorReport() {
        StringBuilder report = new StringBuilder();
        report.append("======================================\n");
        report.append("  INFORMAÇÕES ESTRATÉGICAS - NÍVEL 2\n");
        report.append("======================================\n\n");

        report.append(generatePublicReport());

        report.append("\n***** DADOS RESTRITOS - DIRETORES *****\n\n");

        report.append("Uso de Agrotóxicos:\n");
        report.append("  • Propriedades com uso registrado: 8.456\n");
        report.append("  • Produtos autorizados aplicados: 234 tipos\n");
        report.append("  • Volume total aplicado (2024): 1.234.567 litros\n\n");

        report.append("Fiscalizações Realizadas:\n");
        report.append("  • Total de fiscalizações: 1.245\n");
        report.append("  • Irregularidades encontradas: 234 (18.8%)\n");
        report.append("  • Multas aplicadas: R$ 4.567.890,00\n");
        report.append("  • Processos em andamento: 156\n\n");

        report.append("Impactos Ambientais Médios:\n");
        report.append("  • Contaminação de lençóis freáticos: MODERADA\n");
        report.append("  • Impacto em rios: BAIXA\n");
        report.append("  • Impacto em fauna: MODERADA\n\n");

        report.append("======================================\n");

        return report.toString();
    }

    private String generateMinisterReport() {
        StringBuilder report = new StringBuilder();
        report.append("======================================\n");
        report.append("  INFORMAÇÕES CONFIDENCIAIS - NÍVEL 3\n");
        report.append("======================================\n\n");

        report.append(generateDirectorReport());

        report.append("\n***** DADOS CONFIDENCIAIS - MINISTRO *****\n\n");

        report.append("⚠️ AGROTÓXICOS PROIBIDOS IDENTIFICADOS:\n\n");

        report.append("Substâncias Proibidas em Uso:\n");
        report.append("  1. Paraquat - 45 propriedades\n");
        report.append("     Risco: ALTO | Impacto: Lençóis freáticos comprometidos\n");
        report.append("     Ação: Embargo imediato recomendado\n\n");

        report.append("  2. Carbofurano - 23 propriedades\n");
        report.append("     Risco: CRÍTICO | Impacto: Mortandade de fauna aquática\n");
        report.append("     Ação: Processo criminal iniciado\n\n");

        report.append("  3. Endossulfan - 12 propriedades\n");
        report.append("     Risco: ALTO | Impacto: Contaminação de rios e mares\n");
        report.append("     Ação: Multa aplicada, monitoramento intensificado\n\n");

        report.append("Análises Críticas:\n");
        report.append("  • Total de propriedades irregulares: 80\n");
        report.append("  • Volume de substâncias proibidas: 45.678 litros\n");
        report.append("  • Áreas de risco iminente: 12\n");
        report.append("  • Denúncias em investigação: 34\n\n");

        report.append("Impactos Graves Identificados:\n");
        report.append("  🔴 CRÍTICO: 5 lençóis freáticos contaminados\n");
        report.append("  🔴 CRÍTICO: 3 rios com níveis tóxicos\n");
        report.append("  🟠 ALTO: Mortandade de peixes em 2 regiões\n");
        report.append("  🟠 ALTO: Contaminação detectada em 1 área costeira\n\n");

        report.append("Ações Recomendadas (Urgentes):\n");
        report.append("  ► Interdição imediata de 5 propriedades\n");
        report.append("  ► Abertura de inquérito criminal: 8 casos\n");
        report.append("  ► Convocação de audiência pública\n");
        report.append("  ► Intensificação de fiscalização em 15 municípios\n\n");

        report.append("⚠️ CONFIDENCIAL - USO RESTRITO\n");
        report.append("Documento classificado como SECRETO\n");
        report.append("Portaria MMA nº 001/2024\n\n");

        report.append("======================================\n");

        return report.toString();
    }

    public AccessLevel getMaxAccessLevel(String sessionId) {
        Session session = sessionManager.getSession(sessionId);

        if (session == null || !session.isActive()) {
            return null;
        }

        return session.getUser().getAccessLevel();
    }
}
