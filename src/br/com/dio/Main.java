package br.com.dio;

import br.com.dio.model.Board;
import br.com.dio.model.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static br.com.dio.util.BoardTemplate.BOARD_TEMPLATE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static Board board;
    private static final int BOARD_LIMIT = 9;

    public static void main(String[] args) {
        final Map<String, String> positions = Stream.of(args)
                .map(s -> s.split(";"))
                .filter(arr -> arr.length == 2)
                .collect(toMap(arr -> arr[0], arr -> arr[1]));

        int option = -1;
        while (true) {
            System.out.println("\nSelecione uma das opções a seguir:");
            System.out.println("1 - Iniciar um novo Jogo");
            System.out.println("2 - Colocar um novo número");
            System.out.println("3 - Remover um número");
            System.out.println("4 - Visualizar jogo atual");
            System.out.println("5 - Verificar status do jogo");
            System.out.println("6 - Limpar jogo");
            System.out.println("7 - Finalizar jogo");
            System.out.println("8 - Sair");

            option = runUntilGetValidNumber(1, 8);

            switch (option) {
                case 1:
                    startGame(positions);
                    break;
                case 2:
                    inputNumber();
                    break;
                case 3:
                    removeNumber();
                    break;
                case 4:
                    showCurrentGame();
                    break;
                case 5:
                    showGameStatus();
                    break;
                case 6:
                    clearGame();
                    break;
                case 7:
                    finishGame();
                    break;
                case 8:
                    System.out.println("Saindo do jogo...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opção inválida, selecione uma das opções do menu");
            }
        }
    }

    private static void startGame(final Map<String, String> positions) {
        if (nonNull(board)) {
            System.out.println("O jogo já foi iniciado.");
            return;
        }

        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            List<Space> rowSpaces = new ArrayList<>();
            for (int j = 0; j < BOARD_LIMIT; j++) {
                String key = i + "," + j;
                String positionConfig = positions.get(key);

                if (positionConfig == null || !positionConfig.contains(",")) {
                    positionConfig = "0,false"; // valor padrão
                }

                String[] parts = positionConfig.split(",");
                int expected = 0;
                boolean fixed = false;

                try {
                    expected = Integer.parseInt(parts[0]);
                    fixed = Boolean.parseBoolean(parts[1]);
                } catch (Exception e) {
                    System.out.println("Erro ao ler valor da posição " + key + ". Usando valor padrão.");
                }

                Space currentSpace = new Space(expected, fixed);
                rowSpaces.add(currentSpace);
            }
            spaces.add(rowSpaces);
        }

        board = new Board(spaces);
        System.out.println("O jogo está pronto para começar.");
    }

    private static void inputNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        System.out.println("Informe a coluna (0-8):");
        int col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha (0-8):");
        int row = runUntilGetValidNumber(0, 8);
        System.out.printf("Informe o número (1-9) para a posição [%d,%d]:\n", col, row);
        int value = runUntilGetValidNumber(1, 9);

        if (!board.changeValue(col, row, value)) {
            System.out.printf("A posição [%d,%d] tem um valor fixo.\n", col, row);
        }
    }

    private static void removeNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        System.out.println("Informe a coluna (0-8):");
        int col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha (0-8):");
        int row = runUntilGetValidNumber(0, 8);

        if (!board.clearValue(col, row)) {
            System.out.printf("A posição [%d,%d] tem um valor fixo.\n", col, row);
        }
    }

    private static void showCurrentGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        Object[] args = new Object[81];
        int argPos = 0;
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (List<Space> col : board.getSpaces()) {
                Integer actual = col.get(i).getActual();
                args[argPos++] = " " + (isNull(actual) ? " " : actual.toString());
            }
        }

        System.out.println("Seu jogo se encontra da seguinte forma:");
        System.out.printf((BOARD_TEMPLATE) + "\n", args);
    }

    private static void showGameStatus() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        System.out.printf("O jogo atualmente se encontra no status: %s\n", board.getStatus().getLabel());

        if (board.hasErrors()) {
            System.out.println("O jogo contém erros.");
        } else {
            System.out.println("O jogo não contém erros.");
        }
    }

    private static void clearGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        System.out.println("Tem certeza que deseja limpar o jogo? Digite 'sim' ou 'não':");
        String confirm = scanner.next().trim();

        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")) {
            System.out.println("Informe 'sim' ou 'não':");
            confirm = scanner.next().trim();
        }

        if (confirm.equalsIgnoreCase("sim")) {
            board.reset();
            System.out.println("Jogo limpo com sucesso.");
        } else {
            System.out.println("Ação de limpar cancelada.");
        }
    }

    private static void finishGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }

        if (board.gameIsFinished()) {
            System.out.println("Parabéns! Você concluiu o jogo.");
            showCurrentGame();
            board = null;
        } else if (board.hasErrors()) {
            System.out.println("Seu jogo contém erros. Verifique e ajuste o tabuleiro.");
        } else {
            System.out.println("Ainda há espaços a preencher.");
        }
    }

    private static int runUntilGetValidNumber(final int min, final int max) {
        while (true) {
            try {
                int current = Integer.parseInt(scanner.next());
                if (current >= min && current <= max) {
                    return current;
                }
                System.out.printf("Informe um número entre %d e %d:\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Tente novamente.");
            }
        }
    }
}