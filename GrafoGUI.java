import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GrafoGUI extends JFrame {

    private final List<Ponto> vertices = new ArrayList<>();
    private final List<Aresta> arestas = new ArrayList<>();
    private Ponto primeiroClique = null;

    private final JTextArea outputArea;
    private String modoAtual = "VERTICE";
    private Set<Aresta> arestasDestaque = new HashSet<>();
    private Map<Integer, Color> corComponentes = new HashMap<>();

    public GrafoGUI() {
        setTitle("Editor de Grafos - Ferramenta Didática");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        final PainelGrafo painel = new PainelGrafo();
        add(painel, BorderLayout.CENTER);

        // Área inferior de texto
        outputArea = new JTextArea(8, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        // Painel superior com botões
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));

        String[] nomes = {
            "Novo Vértice", "Nova Aresta", "Passeio", "Trilha",
            "Caminho", "Ciclo", "Conectividade", "Componentes",
            "Matriz", "Arvore Geradora Minima", "Limpar Tudo"
        };

        for (final String nome : nomes) {
            JButton btn = new JButton(nome);
            botoes.add(btn);
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (nome.equals("Novo Vértice")) setModo("VERTICE");
                    else if (nome.equals("Nova Aresta")) setModo("ARESTA");
                    else if (nome.equals("Passeio")) setModo("PASSEIO");
                    else if (nome.equals("Trilha")) setModo("TRILHA");
                    else if (nome.equals("Caminho")) setModo("CAMINHO");
                    else if (nome.equals("Ciclo")) setModo("CICLO");
                    else if (nome.equals("Conectividade")) verificarConectividade();
                    else if (nome.equals("Componentes")) identificarComponentes();
                    else if (nome.equals("Matriz")) mostrarMatrizAdjacencia();
                    else if (nome.equals("Arvore Geradora Minima")) gerarArvoreMinima();
                    else if (nome.equals("Limpar Tudo")) limparTudo(painel);
                    painel.repaint();
                }
            });
        }

        add(botoes, BorderLayout.NORTH);
        setVisible(true);
    }

    private void setModo(String modo) {
        modoAtual = modo;
        primeiroClique = null;
        arestasDestaque.clear();
        corComponentes.clear();
        outputArea.setText("Modo atual: " + modo + "\n");
    }
    
    
	//Nova funcao para o trabalho de grafos
	
	private void gerarArvoreMinima(){
		System.out.println("teste");
	}
	
    private void limparTudo(JPanel painel) {
        vertices.clear();
        arestas.clear();
        corComponentes.clear();
        arestasDestaque.clear();
        primeiroClique = null;
        outputArea.setText("Grafo limpo.\n");
        painel.repaint();
    }

    private void mostrarMatrizAdjacencia() {
        int n = vertices.size();
        double[][] matriz = new double[n][n];
        for (Aresta a : arestas) {
            int i = a.p1.id;
            int j = a.p2.id;
            matriz[i][j] = a.peso;
            matriz[j][i] = a.peso; // não direcionado
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Matriz de Adjacência (ponderada):\n\n");
        sb.append(String.format("%6s", ""));
        for (int i = 0; i < n; i++) sb.append(String.format("%6d", i));
        sb.append("\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%6d", i));
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%6.1f", matriz[i][j]));
            }
            sb.append("\n");
        }
        outputArea.setText(sb.toString());
    }

    private class PainelGrafo extends JPanel {
        public PainelGrafo() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (modoAtual.equals("VERTICE")) {
                        vertices.add(new Ponto(e.getX(), e.getY(), vertices.size()));
                        repaint();
                    } else if (modoAtual.equals("ARESTA")) {
                        selecionarOuCriarAresta(e);
                    } else if (modoAtual.equals("PASSEIO") ||
                               modoAtual.equals("TRILHA") ||
                               modoAtual.equals("CAMINHO") ||
                               modoAtual.equals("CICLO")) {
                        selecionarVerticesParaAnalise(e);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Aresta a : arestas) {
                if (arestasDestaque.contains(a))
                    a.desenhar(g, Color.GREEN.darker(), true);
                else
                    a.desenhar(g, Color.BLACK, false);
            }
            for (Ponto p : vertices) {
                Color cor = corComponentes.getOrDefault(p.id, p.selecionado ? Color.RED : Color.BLUE);
                p.desenhar(g, cor);
            }
        }
    }

    private void selecionarOuCriarAresta(MouseEvent e) {
        for (Ponto p : vertices) {
            if (p.contains(e.getPoint())) {
                if (primeiroClique == null) {
                    primeiroClique = p;
                    p.setSelecionado(true);
                } else if (primeiroClique != p) {
                    String pesoStr = JOptionPane.showInputDialog("Informe o peso da aresta:");
                    try {
                        double peso = Double.parseDouble(pesoStr);
                        boolean existe = false;
                        for (Aresta a : arestas) {
                            if ((a.p1 == primeiroClique && a.p2 == p) || (a.p1 == p && a.p2 == primeiroClique)) {
                                existe = true;
                                break;
                            }
                        }
                        if (!existe) arestas.add(new Aresta(primeiroClique, p, peso));
                        else JOptionPane.showMessageDialog(null, "Aresta já existe!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Peso inválido!");
                    }
                    primeiroClique.setSelecionado(false);
                    primeiroClique = null;
                    repaint();
                }
                return;
            }
        }
    }

    private void selecionarVerticesParaAnalise(MouseEvent e) {
        for (Ponto p : vertices) {
            if (p.contains(e.getPoint())) {
                if (primeiroClique == null) {
                    primeiroClique = p;
                    p.setSelecionado(true);
                } else if (primeiroClique != p) {
                    p.setSelecionado(true);
                    analisarRelacao(primeiroClique, p);
                    primeiroClique.setSelecionado(false);
                    p.setSelecionado(false);
                    primeiroClique = null;
                }
                repaint();
                return;
            }
        }
    }

    private void analisarRelacao(Ponto a, Ponto b) {
        arestasDestaque.clear();
        if (modoAtual.equals("PASSEIO")) {
            outputArea.setText("Passeio: sequência conectada entre " + a.id + " e " + b.id + ".\n");
        } else if (modoAtual.equals("TRILHA")) {
            outputArea.setText("Trilha: sequência sem repetição de arestas entre " + a.id + " e " + b.id + ".\n");
        } else if (modoAtual.equals("CAMINHO")) {
            List<Aresta> caminho = encontrarCaminho(a.id, b.id);
            if (caminho != null) {
                arestasDestaque.addAll(caminho);
                outputArea.setText("Caminho entre " + a.id + " e " + b.id + ": existe.\nArestas: " + caminho + "\n");
            } else {
                outputArea.setText("Não existe caminho entre " + a.id + " e " + b.id + ".\n");
            }
        } else if (modoAtual.equals("CICLO")) {
            boolean existe = existeCiclo(a.id);
            outputArea.setText("Ciclo envolvendo " + a.id + ": " + (existe ? "Existe\n" : "Não existe\n"));
        }
    }

    private List<Aresta> encontrarCaminho(int origem, int destino) {
        Set<Integer> visitados = new HashSet<>();
        List<Aresta> caminho = new ArrayList<>();
        if (dfsCaminho(origem, destino, visitados, caminho)) return caminho;
        return null;
    }

    private boolean dfsCaminho(int atual, int destino, Set<Integer> visitados, List<Aresta> caminho) {
        if (atual == destino) return true;
        visitados.add(atual);
        for (Aresta a : arestas) {
            int vizinho = -1;
            if (a.p1.id == atual) vizinho = a.p2.id;
            else if (a.p2.id == atual) vizinho = a.p1.id;
            if (vizinho != -1 && !visitados.contains(vizinho)) {
                caminho.add(a);
                if (dfsCaminho(vizinho, destino, visitados, caminho)) return true;
                caminho.remove(caminho.size() - 1);
            }
        }
        return false;
    }

    private boolean existeCiclo(int origem) {
        return dfsCiclo(origem, -1, new HashSet<>());
    }

    private boolean dfsCiclo(int atual, int pai, Set<Integer> visitados) {
        visitados.add(atual);
        for (Aresta a : arestas) {
            int vizinho = -1;
            if (a.p1.id == atual) vizinho = a.p2.id;
            else if (a.p2.id == atual) vizinho = a.p1.id;
            if (vizinho == -1) continue;
            if (!visitados.contains(vizinho)) {
                if (dfsCiclo(vizinho, atual, visitados)) {
                    arestasDestaque.add(a);
                    return true;
                }
            } else if (vizinho != pai) {
                arestasDestaque.add(a);
                return true;
            }
        }
        return false;
    }

    private void verificarConectividade() {
        if (vertices.isEmpty()) {
            outputArea.setText("Nenhum vértice no grafo.\n");
            return;
        }
        Set<Integer> visitados = new HashSet<>();
        dfsColeta(vertices.get(0).id, visitados, new ArrayList<Integer>());
        boolean conexo = visitados.size() == vertices.size();
        outputArea.setText(conexo ? "O grafo é CONEXO.\n" : "O grafo NÃO é conexo.\n");
    }

    private void identificarComponentes() {
        Set<Integer> visitados = new HashSet<>();
        List<List<Integer>> componentes = new ArrayList<>();
        corComponentes.clear();

        for (Ponto v : vertices) {
            if (!visitados.contains(v.id)) {
                List<Integer> comp = new ArrayList<>();
                dfsColeta(v.id, visitados, comp);
                componentes.add(comp);
            }
        }

        Random rand = new Random();
        for (List<Integer> comp : componentes) {
            Color c = new Color(rand.nextInt(200), rand.nextInt(200), rand.nextInt(200));
            for (Integer id : comp) corComponentes.put(id, c);
        }

        StringBuilder sb = new StringBuilder("Componentes conexas encontradas:\n");
        int i = 1;
        for (List<Integer> c : componentes)
            sb.append("Componente ").append(i++).append(": ").append(c).append("\n");
        outputArea.setText(sb.toString());
    }

    private void dfsColeta(int atual, Set<Integer> visitados, List<Integer> comp) {
        visitados.add(atual);
        comp.add(atual);
        for (Aresta a : arestas) {
            int vizinho = -1;
            if (a.p1.id == atual) vizinho = a.p2.id;
            else if (a.p2.id == atual) vizinho = a.p1.id;
            if (vizinho != -1 && !visitados.contains(vizinho))
                dfsColeta(vizinho, visitados, comp);
        }
    }

    private static class Ponto {
        int x, y, id;
        boolean selecionado = false;
        static final int RAIO = 20;

        public Ponto(int x, int y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }

        public boolean contains(Point p) {
            return Math.hypot(p.x - x, p.y - y) < RAIO;
        }

        public void setSelecionado(boolean s) {
            this.selecionado = s;
        }

        public void desenhar(Graphics g, Color cor) {
            g.setColor(cor);
            g.fillOval(x - RAIO, y - RAIO, RAIO * 2, RAIO * 2);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(id), x - 5, y + 5);
        }
    }

    private static class Aresta {
        Ponto p1, p2;
        double peso;

        public Aresta(Ponto p1, Ponto p2, double peso) {
            this.p1 = p1;
            this.p2 = p2;
            this.peso = peso;
        }

        public void desenhar(Graphics g, Color cor, boolean grossa) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(grossa ? 3 : 1));
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            int midX = (p1.x + p2.x) / 2;
            int midY = (p1.y + p2.y) / 2;
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.format("%.1f", peso), midX, midY);
        }

        @Override
        public String toString() {
            return "(" + p1.id + "-" + p2.id + ")";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GrafoGUI();
            }
        });
    }
}
