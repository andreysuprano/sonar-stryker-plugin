package br.com.pbtech.issues;

import br.com.pbtech.builder.mutantes.MutantesBuilderForTest;
import br.com.pbtech.model.*;
import br.com.pbtech.rules.js.mutators.JsRule;
import br.com.pbtech.rules.js.mutators.JsRules;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MutantesVivosPosTesteIssueRegisterTest {
    private static SensorContext mockedSensorContext;
    private static NewIssue mockedNewIssue;
    private static InputFile mockedFile;
    private static NewIssueLocation mockedNewIssueLocation;
    private static TextRange mockedTextRange;

    @BeforeEach
    public void setUp() {
        mockedSensorContext = mock(SensorContext.class);
        mockedNewIssue = mock(NewIssue.class);
        mockedFile = mock(InputFile.class);
        mockedNewIssueLocation = mock(NewIssueLocation.class);
        mockedTextRange = mock(TextRange.class);

        when(mockedSensorContext.newIssue()).thenReturn(mockedNewIssue);
        when(mockedNewIssue.forRule(any(RuleKey.class))).thenReturn(mockedNewIssue);
        when(mockedNewIssue.gap(any(Double.class))).thenReturn(mockedNewIssue);
        when(mockedNewIssue.newLocation()).thenReturn(mockedNewIssueLocation);
        when(mockedNewIssue.at(any(NewIssueLocation.class))).thenReturn(mockedNewIssue);
        when(mockedNewIssueLocation.on(any(InputComponent.class))).thenReturn(mockedNewIssueLocation);
        when(mockedNewIssueLocation.at(any(TextRange.class))).thenReturn(mockedNewIssueLocation);
        when(mockedNewIssueLocation.message(any(String.class))).thenReturn(mockedNewIssueLocation);
        when(mockedFile.newRange(any(Integer.class), any(Integer.class), any(Integer.class), any(Integer.class))).thenReturn(mockedTextRange);
    }

    @Test
    public void DeveCriarIssueParaOArquivoComARegra_MUTANTE_VIVO_POS_TESTE() {
        List<Mutant> mutantes = MutantesBuilderForTest.criaListaDeMutantes()
                .withAKilledMutant()
                .withASurvivedMutant()
                .withATimeoutMutant()
                .build();

        MutantesVivosPosTesteIssueRegister mutantesVivosPosTesteIssueRegister = new MutantesVivosPosTesteIssueRegister();
        mutantesVivosPosTesteIssueRegister.registrarIssue(mockedSensorContext, mockedFile, mutantes);

        JsRule jsRule = JsRules.getRuleByMutatorName(MutatorName.BlockStatement);

        verify(mockedSensorContext, times(1)).newIssue();
        verify(mockedNewIssue, times(1)).forRule(jsRule.getOperatorJs());
        verify(mockedNewIssue, times(1)).gap(jsRule.getGap());
        verify(mockedNewIssue, times(1)).newLocation();
        verify(mockedNewIssue, times(1)).at(mockedNewIssueLocation);
        verify(mockedNewIssue, times(1)).save();
        verify(mockedNewIssueLocation, times(1)).on(mockedFile);
        verify(mockedNewIssueLocation, times(1)).at(any(TextRange.class));
        verify(mockedNewIssueLocation, times(1)).message(any(String.class));
    }

    @Test
    public void DeveVerificarQueNaoExistemMutantesVivosELancarUmaExcecao() {

        List<Mutant> mutantes = MutantesBuilderForTest.criaListaDeMutantes()
                .withAKilledMutant()
                .withAKilledMutant()
                .withATimeoutMutant()
                .build();

        MutantesVivosPosTesteIssueRegister mutantesVivosPosTesteIssueRegister = new MutantesVivosPosTesteIssueRegister();

        assertThrows(NotMutationSurvivedException.class, () -> mutantesVivosPosTesteIssueRegister.registrarIssue(mockedSensorContext, mockedFile, mutantes));
    }

    @Test
    @DisplayName("DeveCriarIssueParaOArquivoComARegra_MUTANTE_VIVO_POS_TESTE_NasLinhasOnde")
    public void DeveCriarIssueParaOArquivoComARegra_MUTANTE_VIVO_POS_TESTE_NasLinhasOnde() {

        ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

        Mutant mutantSurvived = new Mutant(
                "02",
                new Location(
                        new End(1, 11),
                        new Start(1, 9)
                ),
                MutatorName.BlockStatement,
                "{}",
                MutantStatus.SURVIVED,
                "Killed by: Grafo Tests construtor A quantidade mínima de vertices definidos deve ser maior que 1"
        );

        List<Mutant> mutantes = MutantesBuilderForTest.criaListaDeMutantes()
                .withATimeoutMutant()
                .withAKilledMutant()
                .withThisMutant(mutantSurvived)
                .build();

        MutantesVivosPosTesteIssueRegister mutantesVivosPosTesteIssueRegister = new MutantesVivosPosTesteIssueRegister();
        mutantesVivosPosTesteIssueRegister.registrarIssue(mockedSensorContext, mockedFile, mutantes);

        verify(mockedFile).newRange(integerArgumentCaptor.capture(),integerArgumentCaptor.capture(), integerArgumentCaptor.capture(), integerArgumentCaptor.capture());

        List<Integer> argumentosPassados = integerArgumentCaptor.getAllValues();
        assertAll(
                () -> assertEquals(argumentosPassados.get(0), 9),
                () -> assertEquals(argumentosPassados.get(1), 0),
                () -> assertEquals(argumentosPassados.get(2), 11),
                () -> assertEquals(argumentosPassados.get(3), 0)
                );
    }
}
