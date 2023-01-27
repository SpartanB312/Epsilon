package club.eridani.epsilon.client.gui

interface IFatherComponent : IComponent {
    var isActive: Boolean
    var children: MutableList<IChildComponent>
}