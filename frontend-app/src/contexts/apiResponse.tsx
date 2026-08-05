/*
 Este tipo define como deve ser esperado a resposta do back-end, para que o front-end possa lidar com ela de forma consistente. 
 <T> é um tipo genérico que permite que a resposta contenha qualquer tipo de dado, dependendo da requisição feita. 
*/

export type ApiResponse<T> = {
    sucesso: boolean;
    mensagem: string;
    dado: T;
}