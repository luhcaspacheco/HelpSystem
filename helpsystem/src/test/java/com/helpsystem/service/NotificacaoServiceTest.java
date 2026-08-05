package com.helpsystem.service;

import com.helpsystem.model.Notificacao;
import com.helpsystem.model.Usuario;
import com.helpsystem.repository.NotificacaoRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoServiceTest {

    private final NotificacaoRepository notificacaoRepository = mock(NotificacaoRepository.class);
    private final NotificacaoService service = new NotificacaoService(notificacaoRepository);

    @Test
    void bloqueiaMarcarNotificacaoDeOutroUsuarioComoLida() {
        Usuario dono = new Usuario();
        dono.setId(1);

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2);

        Notificacao notificacao = new Notificacao();
        notificacao.setId(5);
        notificacao.setUsuario(dono);

        when(notificacaoRepository.findById(5)).thenReturn(Optional.of(notificacao));

        ResultadoOperacao resultado = service.marcarComoLida(5, outroUsuario);

        assertFalse(resultado.isSucesso());
        verify(notificacaoRepository, never()).save(notificacao);
    }
}
